package org.cmdbuild.services.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.DomainFactory;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ICard.CardAttributes;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.interfaces.ProcessTypeFactory;
import org.cmdbuild.elements.interfaces.RelationFactory;
import org.cmdbuild.elements.wrappers.GroupCard;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.logic.DataAccessLogic;
import org.cmdbuild.logic.LogicDTO.Card;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetRelationList.DomainInfo;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;

public class UserContext {

	private static class GroupsManager {

		private final Map<Integer, Group> groupsById;
		private final Map<String, Group> groupsByName;
		private Group defaultGroup;

		private GroupsManager() {
			groupsById = new HashMap<Integer, Group>();
			groupsByName = new HashMap<String, Group>();
			defaultGroup = null;
		}

		public int size() {
			return groupsById.size();
		}

		public boolean contains(final int id) {
			return groupsById.containsKey(id);
		}

		public boolean contains(final String name) {
			return groupsByName.containsKey(name);
		}

		public Collection<Group> values() {
			return groupsById.values();
		}

		public void add(final Group group) {
			groupsById.put(group.getId(), group);
			groupsByName.put(group.getName(), group);
		}

		public void clear() {
			groupsById.clear();
			groupsByName.clear();
		}

		public Group get(final int id) {
			return groupsById.get(id);
		}

		public Group get(final String name) {
			return groupsByName.get(name);
		}

		public boolean hasDefaultGroup() {
			return (defaultGroup != null);
		}

		public Group getDefaultGroup() {
			return defaultGroup;
		}

		public void setDefaultGroup(final Group group) {
			defaultGroup = group;
		}

		public static GroupsManager getInstance(final Group group) {
			return getInstance(Arrays.asList(group));
		}

		public static GroupsManager getInstance(final Iterable<Group> groups) {
			final GroupsManager _groups = new GroupsManager();
			for (final Group group : groups) {
				if (group.isDefault()) {
					_groups.setDefaultGroup(group);
				}
				_groups.add(group);
			}
			if (!_groups.hasDefaultGroup() && _groups.size() == 1) {
				final Group group = _groups.values().iterator().next();
				_groups.setDefaultGroup(group);
			}
			return _groups;
		}

	}

	private final User user;
	private final UserType userType;
	private final String requestedUsername;
	private final GroupsManager groups;
	private PrivilegeManager privilegeManager;
	private FactoryManager factoryManager;
	private Authenticator authenticator;

	public UserContext(final User user, final String requestedUsername) {
		this.user = user;
		this.userType = getUserType(user, requestedUsername);
		this.requestedUsername = requestedUsername;
		if (isSystemUser(user)) {
			final Group group = GroupImpl.getSystemGroup();
			this.groups = GroupsManager.getInstance(group);
			this.privilegeManager = new PrivilegeManager(group);
		} else {
			final Iterable<Group> groups = loadGroups(userType, user, requestedUsername);
			this.groups = GroupsManager.getInstance(groups);
			this.privilegeManager = createPrivilegeManager(groups);
		}
	}

	private static Iterable<Group> loadGroups(final UserType userType, final User user, final String requestedUsername) {
		final Iterable<Group> groups;
		switch (userType) {
		case DOMAIN:
			groups = loadGroupsAndPrivilegesForDomainUser(requestedUsername);
			break;
		default:
			groups = AuthenticationFacade.getGroupListForUser(user.getId());
			break;
		}
		return groups;
	}

	private static Iterable<Group> loadGroupsAndPrivilegesForDomainUser(final String username) {
		final ICard card = DomainUserUtils.queryDomainUser(username).getCard();
		final String domain = DomainUserUtils.MetadataUtils.getGroupTable();

		final DataAccessLogic dataAccesslogic = TemporaryObjectsBeforeSpringDI.getSystemDataAccessLogic();
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

		return groups;
	}

	private static PrivilegeManager createPrivilegeManager(final Iterable<Group> groups) {
		final PrivilegeManager privilegeManager = new PrivilegeManager();
		for (final Group group : groups) {
			privilegeManager.addGroupPrivileges(group);
		}
		return privilegeManager;
	}

	public UserContext(final User user) {
		this(user, null);
	}

	public static boolean isSystemUser(final User user) {
		return UserImpl.getSystemUser().getId() == user.getId();
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
		return groups.getDefaultGroup();
	}

	public boolean hasDefaultGroup() {
		return groups.hasDefaultGroup();
	}

	public Group getWFStartGroup() {
		return getDefaultGroup();
	}

	public void assureNotNullDefaultGroup() {
		if (!groups.hasDefaultGroup()) {
			throw AuthExceptionType.AUTH_MULTIPLE_GROUPS.createException();
		}
	}

	public void setDefaultGroup(final int groupId) {
		setSingleGroupAndPrivileges(groups.get(groupId));
	}

	public void setDefaultGroup(final String groupName) {
		final Group g = groups.get(groupName);
		if (g != null) {
			setSingleGroupAndPrivileges(g);
		}
	}

	private void setSingleGroupAndPrivileges(final Group group) {
		groups.clear();
		groups.add(group);
		groups.setDefaultGroup(group);
		privilegeManager = new PrivilegeManager(group);
	}

	public Collection<Group> getGroups() {
		return groups.values();
	}

	// Needed because of report privileges
	public boolean belongsTo(final int groupId) {
		return groups.contains(groupId);
	}

	public boolean belongsTo(final String groupName) {
		return groups.contains(groupName);
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