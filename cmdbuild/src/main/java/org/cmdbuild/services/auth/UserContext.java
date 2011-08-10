package org.cmdbuild.services.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.DomainFactory;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.interfaces.ProcessTypeFactory;
import org.cmdbuild.elements.interfaces.RelationFactory;
import org.cmdbuild.elements.interfaces.ICard.CardAttributes;
import org.cmdbuild.elements.wrappers.GroupCard;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.logic.DataAccessLogic;
import org.cmdbuild.logic.LogicDTO.Card;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetRelationList.DomainInfo;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;

public class UserContext {

	private final User user;
	private final UserType userType;
	private Group defaultGroup = null;
	private final Set<Group> groups = new HashSet<Group>();
	private final Map<Integer, Group> groupsById = new HashMap<Integer, Group>();
	private final Map<String, Group> groupsByName = new HashMap<String, Group>();
	private PrivilegeManager privilegeManager = new PrivilegeManager();
	private FactoryManager factoryManager = null;
	private final String requestedUsername;
	private Authenticator authenticator = null;

	public UserContext(final User user, final String requestedUsername) {
		this.userType = getUserType(user, requestedUsername);
		this.requestedUsername = requestedUsername;
		if (isSystemUser(user)) {
			this.user = UserImpl.getSystemUser();
			setSingleGroupAndPrivileges(GroupImpl.getSystemGroup());
		} else {
			this.user = user;
			loadGroupsAndPrivileges();
		}
	}

	public UserContext(final User user) {
		this(user, null);
	}

	public static boolean isSystemUser(final User user) {
		return UserImpl.getSystemUser().equals(user);
	}

	private static UserType getUserType(final User user, final String requestedUsername) {
		final UserType userType;
		if (isSystemUser(user) || (requestedUsername == null)) {
			userType = UserType.APPLICATION;
		} else if (isInUserTable(requestedUsername)) {
			userType = UserType.DOMAIN;
		} else {
			userType = UserType.GUEST;
		}
		return userType;
	}

	private static boolean isInUserTable(final String login) {
		return DomainUserUtils.queryDomainUser(login).isFound();
	}

	public static UserContext systemContext() {
		final User user = UserImpl.getSystemUser();
		return new UserContext(user);
	}

	private void setSingleGroupAndPrivileges(final Group group) {
		defaultGroup = group;
		clearGroups();
		addGroup(group);
		privilegeManager = new PrivilegeManager(group);
	}

	private void clearGroups() {
		groups.clear();
		groupsById.clear();
		groupsByName.clear();
	}

	private void addGroup(final Group group) {
		groups.add(group);
		groupsById.put(group.getId(), group);
		groupsByName.put(group.getName(), group);
	}

	private void loadGroupsAndPrivileges() {
		switch (userType) {
		case DOMAIN:
			loadGroupsAndPrivilegesForDomainUser();
			break;
		default:
			loadGroupsAndPrivilegesForSystemUser();
			break;
		}
	}

	private void loadGroupsAndPrivilegesForSystemUser() {
		final Iterable<Group> groups = AuthenticationFacade.getGroupListForUser(user.getId());
		loadGroupsAndPrivileges(groups);
	}

	private void loadGroupsAndPrivilegesForDomainUser() {
		final ICard card = DomainUserUtils.queryDomainUser(requestedUsername).getCard();
		final String domain = DomainUserUtils.MetadataUtils.getGroupTable();

		final DataAccessLogic dataAccesslogic = new DataAccessLogic(UserContext.systemContext());
		final Card src = new Card(card.getSchema().getId(), card.getId());
		final DomainWithSource dom = DomainWithSource.create(null, null);
		final GetRelationListResponse out = dataAccesslogic.getRelationList(src, dom);

		final List<Group> groups = new ArrayList<Group>();

		for (final DomainInfo di : out) {
			final String domainName = di.getQueryDomain().getDomain().getName();
			if (domainName.equals(domain)) {
				final List<Object> idList = new ArrayList<Object>();
				final String targetClassName = di.getQueryDomain().getTargetClass().getName();
				for (final RelationInfo ri : di) {
					idList.add(ri.getTargetId());
				}
				final ITable targetClassTable = UserContext.systemContext().tables().get(targetClassName);
				final CardQuery targetClassQuery = targetClassTable.cards().list() //
						.attributes(CardAttributes.Id.toString()) //
						.filter(CardAttributes.Id.toString(), AttributeFilterType.EQUALS, idList.toArray());

				for (final ICard targetCard : targetClassQuery) {
					final int id = targetCard.getId();
					final UserContext systemContext = UserContext.systemContext();
					final GroupCard groupCard = GroupCard.get(id, systemContext);
					final Group group = groupCard.toGroup(false);
					groups.add(group);
				}
			}
		}

		loadGroupsAndPrivileges(groups);
	}

	private void loadGroupsAndPrivileges(final Iterable<Group> groups) {
		for (final Group group : groups) {
			if (group.isDefault()) {
				defaultGroup = group;
			}
			addGroup(group);
			privilegeManager.addGroupPrivileges(group);
		}
		if (defaultGroup == null && groupsById.size() == 1) {
			defaultGroup = groupsById.values().iterator().next();
		}
	}

	public User getUser() {
		return user;
	}

	public String getRequestedUsername() {
		if (isGuest()) {
			return requestedUsername;
		} else {
			return user.getName();
		}
	}

	/**
	 * @deprecated Replaced by {@code getUserType()}.
	 */
	@Deprecated
	public boolean isGuest() {
		return !getUserType().equals(UserType.APPLICATION);
	}

	public UserType getUserType() {
		return userType;
	}

	public Group getDefaultGroup() {
		assureNotNullDefaultGroup();
		return defaultGroup;
	}

	public boolean hasDefaultGroup() {
		return (defaultGroup != null);
	}

	public Group getWFStartGroup() {
		return getDefaultGroup();
	}

	public void assureNotNullDefaultGroup() {
		if (defaultGroup == null) {
			throw AuthExceptionType.AUTH_MULTIPLE_GROUPS.createException();
		}
	}

	public void setDefaultGroup(final int groupId) {
		setSingleGroupAndPrivileges(groupsById.get(groupId));
	}

	public void setDefaultGroup(final String groupName) {
		final Group g = groupsByName.get(groupName);
		if (g != null) {
			setSingleGroupAndPrivileges(g);
		}
	}

	public Collection<Group> getGroups() {
		return groupsById.values();
	}

	// Needed because of report privileges
	public boolean belongsTo(final int groupId) {
		return groupsById.containsKey(groupId);
	}

	public boolean belongsTo(final String groupName) {
		return groupsByName.containsKey(groupName);
	}

	public String getUsername() {
		return user.getName();
	}

	public PrivilegeManager privileges() {
		return privilegeManager;
	}

	public ITableFactory tables() {
		return getFactoryManager().getTableFactory();
	}

	public DomainFactory domains() {
		return getFactoryManager().getDomainFactory();
	}

	public RelationFactory relations() {
		return getFactoryManager().getRelationFactory();
	}

	public ProcessTypeFactory processTypes() {
		return getFactoryManager().getProcessTypeFactory();
	}

	private FactoryManager getFactoryManager() {
		if (factoryManager == null) {
			factoryManager = new FactoryManager(this);
		}
		return factoryManager;
	}

	public void setAuthenticator(final Authenticator authenticator) {
		this.authenticator = authenticator;
	}

	public void changePassword(final String oldPassword, final String newPassword) {
		if (authenticator == null) {
			throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
		}
		authenticator.changePassword(getUsername(), oldPassword, newPassword);
	}

	public boolean canChangePassword() {
		return (authenticator != null) && (authenticator.canChangePassword());
	}

	public boolean allowsPasswordLogin() {
		return (authenticator != null) && (authenticator.allowsPasswordLogin());
	}
}
