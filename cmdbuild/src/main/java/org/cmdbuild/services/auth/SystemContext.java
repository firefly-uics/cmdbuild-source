package org.cmdbuild.services.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cmdbuild.auth.acl.CMPrivilegedObject;

public class SystemContext extends UserContext {

	private class SystemPrivilegeManager implements PrivilegeManager {

		@Override
		public void assureAdminPrivilege() {
		}

		@Override
		public void assureCreatePrivilege(final CMPrivilegedObject domain) {
		}

		@Override
		public void assureReadPrivilege(final CMPrivilegedObject domain) {
		}

		@Override
		public void assureWritePrivilege(final CMPrivilegedObject domain) {
		}

		@Override
		public PrivilegeType getPrivilege(final CMPrivilegedObject schema) {
			return PrivilegeType.WRITE;
		}

		@Override
		public boolean hasCreatePrivilege(final CMPrivilegedObject domain) {
			return true;
		}

		@Override
		public boolean hasReadPrivilege(final CMPrivilegedObject domain) {
			return true;
		}

		@Override
		public boolean hasWritePrivilege(final CMPrivilegedObject domain) {
			return true;
		}

		@Override
		public boolean isAdmin() {
			return true;
		}
	}

	private static SystemContext INSTANCE = new SystemContext();

	private SystemContext() {
	}

	public static UserContext getInstance() {
		return INSTANCE;
	}

	@Override
	public boolean belongsTo(final String groupName) {
		return false;
	}

	@Override
	public boolean canChangePassword() {
		return false;
	}

	@Override
	public boolean allowsPasswordLogin() {
		return false;
	}

	@Override
	public void changePassword(final String oldPassword, final String newPassword) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Group getDefaultGroup() {
		return GroupImpl.getSystemGroup();
	}

	@Override
	public Collection<Group> getGroups() {
		final List<Group> groups = new ArrayList<Group>(1);
		groups.add(GroupImpl.getSystemGroup());
		return groups;
	}

	@Override
	public String getRequestedUsername() {
		return getUsername();
	}

	@Override
	public User getUser() {
		return UserImpl.getSystemUser();
	}

	@Override
	public UserType getUserType() {
		return UserType.APPLICATION;
	}

	@Override
	public String getUsername() {
		return getUser().getName();
	}

	@Override
	public Group getWFStartGroup() {
		return getDefaultGroup();
	}

	@Override
	public boolean hasDefaultGroup() {
		return true;
	}

	@Override
	public boolean isGuest() {
		return false;
	}

	@Override
	public PrivilegeManager privileges() {
		return new SystemPrivilegeManager();
	}

}
