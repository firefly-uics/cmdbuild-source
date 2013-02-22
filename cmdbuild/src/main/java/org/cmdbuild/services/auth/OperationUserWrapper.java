package org.cmdbuild.services.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.CMPrivilegedObject;
import org.cmdbuild.auth.acl.DefaultPrivileges;
import org.cmdbuild.auth.acl.PrivilegePair;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.wrappers.PrivilegeCard.PrivilegeType;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.model.profile.UIConfiguration;

public class OperationUserWrapper extends UserContext {

	private class PrivilegeManagerWrap implements PrivilegeManager {

		@Override
		public PrivilegeType getPrivilege(final CMPrivilegedObject schema) {
			if (hasWritePrivilege(schema)) {
				return PrivilegeType.WRITE;
			} else if (hasReadPrivilege(schema)) {
				return PrivilegeType.READ;
			} else {
				return PrivilegeType.NONE;
			}
		}

		@Override
		public boolean hasReadPrivilege(final CMPrivilegedObject table) {
			return user.hasPrivilege(DefaultPrivileges.READ, table);
		}

		@Override
		public boolean hasWritePrivilege(final CMPrivilegedObject table) {
			return user.hasPrivilege(DefaultPrivileges.WRITE, table);
		}

		@Override
		public void assureReadPrivilege(final CMPrivilegedObject table) {
			assurePrivilege(DefaultPrivileges.READ, table);
		}

		@Override
		public void assureWritePrivilege(final CMPrivilegedObject table) {
			assurePrivilege(DefaultPrivileges.WRITE, table);
		}

		private void assurePrivilege(final CMPrivilege requested, final CMPrivilegedObject table) {
			if (!user.hasPrivilege(requested, table)) {
				throw AuthExceptionType.AUTH_CLASS_NOT_AUTHORIZED.createException(table.getPrivilegeId());
			}
		}

		@Override
		public void assureCreatePrivilege(final CMPrivilegedObject table) {
			if (!hasCreatePrivilege(table)) {
				throw AuthExceptionType.AUTH_CLASS_NOT_AUTHORIZED.createException(table.getPrivilegeId());
			}
		}

		@Override
		public boolean hasCreatePrivilege(final CMPrivilegedObject domain) {
			return hasWritePrivilege(domain);
		}

		@Override
		public boolean isAdmin() {
			return user.hasAdministratorPrivileges();
		}

		@Override
		public void assureAdminPrivilege() {
			if (!isAdmin())
				throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
		}
	}

	private final OperationUser user;
	private final AuthenticationLogic authLogic;
	private Group cachedGroup;

	public OperationUserWrapper(final OperationUser user) {
		this.user = user;
		this.authLogic = TemporaryObjectsBeforeSpringDI.getAuthenticationLogic();
	}

	@Override
	public boolean belongsTo(final String groupName) {
		return getCMGroupByName(groupName) != null;
	}

	@Override
	public boolean canChangePassword() {
		return user.getAuthenticatedUser().canChangePassword();
	}

	@Override
	public boolean allowsPasswordLogin() {
		return user.getAuthenticatedUser().canChangePassword(); // Not really
	}

	@Override
	public void changePassword(final String oldPassword, final String newPassword) {
		user.getAuthenticatedUser().changePassword(oldPassword, newPassword);
	}

	@OldDao
	@Override
	public Group getDefaultGroup() {
		if (cachedGroup == null) {
			cachedGroup = getGroupByName(user.getPreferredGroup().getName());
		}
		return cachedGroup;
	}

	@OldDao
	@Override
	public Collection<Group> getGroups() {
		final Collection<String> groupNames = user.getAuthenticatedUser().getGroupNames();
		final List<Group> groups = new ArrayList<Group>(groupNames.size());
		for (final String groupName : groupNames) {
			final Group g = groupFromCMGroup(authLogic.getGroupWithName(groupName));
			groups.add(g);
		}
		return groups;
	}

	@Override
	public String getRequestedUsername() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public User getUser() {
		return new User() {

			@Override
			public String getDescription() {
				return user.getAuthenticatedUser().getDescription();
			}

			@Override
			public String getEncryptedPassword() {
				throw new UnsupportedOperationException("Should never be called!");
			}

			@Override
			public int getId() {
				final Long id = user.getAuthenticatedUser().getId();
				if (id == null) {
					return 0;
				} else {
					return id.intValue();
				}
			}

			@Override
			public String getName() {
				return user.getAuthenticatedUser().getUsername();
			}
		};
	}

	@OldDao
	@Override
	public UserType getUserType() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public String getUsername() {
		return user.getAuthenticatedUser().getUsername();
	}

	@OldDao
	@Override
	public Group getWFStartGroup() {
		return this.getDefaultGroup();
	}

	@Override
	public boolean hasDefaultGroup() {
		return (user.getAuthenticatedUser().getDefaultGroupName() != null);
	}

	@Override
	public boolean isGuest() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public PrivilegeManager privileges() {
		return new PrivilegeManagerWrap();
	}

	/*
	 * Utils and conversion
	 */

	@OldDao
	private Group getGroupByName(final String groupName) {
		return groupFromCMGroup(getCMGroupByName(groupName));
	}

	@OldDao
	private Group groupFromCMGroup(final CMGroup group) {
		if (group == null) {
			return null;
		}
		final Group g = new Group() {

			@Override
			public String getDescription() {
				return group.getDescription();
			}

			@Override
			public UIConfiguration getUIConfiguration() {
				final UIConfiguration uiConfiguration = new UIConfiguration();
				uiConfiguration.setDisabledModules(disabledModules());
				return uiConfiguration;
			}

			private String[] disabledModules() {
				final Set<String> dm = group.getDisabledModules();
				return dm.toArray(new String[dm.size()]);
			}

			@Override
			public int getId() {
				final Long id = user.getAuthenticatedUser().getId();
				if (id == null) {
					return 0;
				} else {
					return id.intValue();
				}
			}

			@Override
			public String getName() {
				return group.getName();
			}

			@Override
			public ITable getStartingClass() {
				// TODO... implements this
				// final Long scid = cmg.getStartingClassId();
				// if (scid != null) {
				// return tables().get(scid.intValue());
				// }
				return null;
			}

			@Override
			public boolean isAdmin() {
				for (final PrivilegePair pp : group.getAllPrivileges()) {
					pp.privilege.implies(DefaultPrivileges.GOD);
				}
				return false;
			}

			@Override
			public boolean isDefault() {
				throw new UnsupportedOperationException("Not implemented yet");
			}

			@Override
			public boolean isCloudAdmin() {
				// FIXME implement me
				return false;
			}
		};
		return g;
	}

	private CMGroup getCMGroupByName(final String groupName) {
		if (groupName == null) {
			return null;
		}
		for (final String name : user.getAuthenticatedUser().getGroupNames()) {
			if (name.equals(groupName)) {
				// ugly but necessary for now...
				return authLogic.getGroupWithName(name);
			}
		}
		return null;
	}
}
