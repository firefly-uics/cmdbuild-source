package org.cmdbuild.services.auth;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cmdbuild.elements.interfaces.DomainFactory;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.interfaces.ProcessTypeFactory;
import org.cmdbuild.elements.interfaces.RelationFactory;
import org.cmdbuild.exception.AuthException.AuthExceptionType;

public class UserContext {

	private User user;
	private Group defaultGroup = null;
	private final Set<Group> groups = new HashSet<Group>();
	private final Map<Integer, Group> groupsById = new HashMap<Integer, Group>();
	private final Map<String, Group> groupsByName = new HashMap<String, Group>();
	private PrivilegeManager privilegeManager = new PrivilegeManager();
	private FactoryManager factoryManager = null;
	private String requestedUsername;
	private Authenticator authenticator = null;

	public UserContext(User user, String requestedUsername) {
		initUser(user);
		this.requestedUsername = requestedUsername;
	}

	public UserContext(User user) {
		initUser(user);
		this.requestedUsername = null; // checked in getRequestedUsername()
	}

	private void initUser(User user) {
		if (UserImpl.getSystemUser().equals(user)) {
			initForSystemUser();
		} else {
			initForRealUser(user);
		}
	}

	private void initForSystemUser() {
		this.user = UserImpl.getSystemUser();
		setSingleGroupAndPrivileges(GroupImpl.getSystemGroup());
	}

	private void initForRealUser(User user) {
		this.user = user;
		loadGroupsAndPrivileges();
	}

	static public UserContext systemContext() {
		return new UserContext();
	}

	// System user
	private UserContext() {
		initForSystemUser();
	}

	private void setSingleGroupAndPrivileges(Group group) {
		defaultGroup = group;
		removeGroups();
		addGroup(group);
		privilegeManager = new PrivilegeManager(group);
	}

	private void removeGroups() {
		groups.clear();
		groupsById.clear();
		groupsByName.clear();
	}

	private void addGroup(Group group) {
		groups.add(group);
		groupsById.put(group.getId(), group);
		groupsByName.put(group.getName(), group);
	}

	private void loadGroupsAndPrivileges() {
		for (Group g : AuthenticationFacade.getGroupListForUser(user.getId())) {
			if (g.isDefault()) {
				defaultGroup = g;
			}
			addGroupAndPrivileges(g);
		}
		if (defaultGroup == null && groupsById.size() == 1) {
			defaultGroup = groupsById.values().iterator().next();
		}
	}

	private void addGroupAndPrivileges(Group g) {
		addGroup(g);
		privilegeManager.addGroupPrivileges(g);
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

	public boolean isGuest() {
		return requestedUsername != null;
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

	public void setDefaultGroup(int groupId) {
		setSingleGroupAndPrivileges(groupsById.get(groupId));
	}

	public void setDefaultGroup(String groupName) {
		Group g = groupsByName.get(groupName);
		if (g != null) {
			setSingleGroupAndPrivileges(g);
		}
	}

	public Collection<Group> getGroups() {
		return groupsById.values();
	}

	// Needed because of report privileges
	public boolean belongsTo(int groupId) {
		return groupsById.containsKey(groupId);
	}

	public boolean belongsTo(String groupName) {
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

	public void setAuthenticator(Authenticator authenticator) {
		this.authenticator = authenticator;
	}

	public void changePassword(String oldPassword, String newPassword) {
		if (authenticator == null) {
			throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
		}
		authenticator.changePassword(getUsername(), oldPassword, newPassword);
	}
	
	public boolean canChangePassword() {
		return (authenticator != null) && (authenticator.canChangePassword());
	}
}
