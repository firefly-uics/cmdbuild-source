package org.cmdbuild.services.auth;

import java.util.Collection;

import org.cmdbuild.elements.interfaces.DomainFactory;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.interfaces.ProcessTypeFactory;
import org.cmdbuild.elements.interfaces.RelationFactory;

public abstract class UserContext {

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

	public abstract ITableFactory tables();

	public abstract DomainFactory domains();

	public abstract RelationFactory relations();

	public abstract ProcessTypeFactory processTypes();

	public abstract void changePassword(final String oldPassword, final String newPassword);

	public abstract boolean canChangePassword();

	public abstract boolean allowsPasswordLogin();

	public static UserContext systemContext() {
		return SystemContext.getInstance();
	}
}