package org.cmdbuild.services.auth;

import java.util.Collection;

public abstract class UserContext {

	public static UserContext systemContext = SystemContext.getInstance();

	public abstract User getUser();

	public abstract String getRequestedUsername();

	/**
	 * @deprecated Replaced by {@code getUserType()}.
	 */
	@Deprecated
	public abstract boolean isGuest();

	public abstract UserType getUserType();

	public abstract Group getDefaultGroup();

	public abstract boolean hasDefaultGroup();

	public abstract Group getWFStartGroup();

	public abstract Collection<Group> getGroups();

	public abstract boolean belongsTo(final String groupName);

	public abstract String getUsername();

	public abstract PrivilegeManager privileges();

	public abstract void changePassword(final String oldPassword, final String newPassword);

	public abstract boolean canChangePassword();

	public abstract boolean allowsPasswordLogin();

	@Deprecated
	public static UserContext systemContext() {
		return systemContext;
	}
}