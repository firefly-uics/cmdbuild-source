package org.cmdbuild.services.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.cmdbuild.auth.AuthenticatedUser;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.DefaultPrivileges;
import org.cmdbuild.auth.acl.PrivilegeSet;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.DomainFactory;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.interfaces.ProcessTypeFactory;
import org.cmdbuild.elements.interfaces.RelationFactory;
import org.cmdbuild.elements.wrappers.PrivilegeCard.PrivilegeType;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.model.profile.UIConfiguration;

public class AuthenticatedUserWrapper extends UserContext {

	private class PrivilegeManagerWrap implements PrivilegeManager {

		@Override
		public PrivilegeType getPrivilege(BaseSchema schema) {
			if (hasWritePrivilege(schema)) {
				return PrivilegeType.WRITE;
			} else if (hasReadPrivilege(schema)) {
				return PrivilegeType.READ;
			} else {
				return PrivilegeType.NONE;
			}
		}

		@Override
		public boolean hasReadPrivilege(BaseSchema schema) {
			return user.hasPrivilege(DefaultPrivileges.READ, schema);
		}

		private boolean hasWritePrivilege(BaseSchema schema) {
			return user.hasPrivilege(DefaultPrivileges.WRITE, schema);
		}

		@Override
		public boolean hasReadPrivilege(ITable table) {
			return user.hasPrivilege(DefaultPrivileges.READ, table);
		}

		@Override
		public boolean hasWritePrivilege(ITable table) {
			return user.hasPrivilege(DefaultPrivileges.WRITE, table);
		}

		@Override
		public void assureReadPrivilege(ITable table) {
			assurePrivilege(DefaultPrivileges.READ, table);
		}

		@Override
		public void assureWritePrivilege(ITable table) {
			assurePrivilege(DefaultPrivileges.WRITE, table);
		}

		private void assurePrivilege(CMPrivilege requested, ITable table) {
			if (!user.hasPrivilege(requested, table)) {
				throw AuthExceptionType.AUTH_CLASS_NOT_AUTHORIZED.createException(table.getName());
			}
		}

		@Override
		public boolean hasReadPrivilege(IDomain domain) {
			return user.hasPrivilege(DefaultPrivileges.READ, domain);
		}

		@Override
		public boolean hasWritePrivilege(IDomain domain) {
			return user.hasPrivilege(DefaultPrivileges.WRITE, domain);
		}

		@Override
		public void assureReadPrivilege(IDomain domain) {
			assurePrivilege(DefaultPrivileges.READ, domain);
		}

		@Override
		public void assureWritePrivilege(IDomain domain) {
			assurePrivilege(DefaultPrivileges.WRITE, domain);
		}

		private void assurePrivilege(CMPrivilege requested, IDomain domain) {
			if (!user.hasPrivilege(requested, domain)) {
				throw AuthExceptionType.AUTH_DOMAIN_NOT_AUTHORIZED.createException(domain.getName());
			}
		}

		@Override
		public void assureCreatePrivilege(ITable table) {
			if (!hasCreatePrivilege(table)) {
				throw AuthExceptionType.AUTH_CLASS_NOT_AUTHORIZED.createException(table.getName());
			}
		}

		@Override
		public void assureCreatePrivilege(IDomain domain) {
			if (!hasCreatePrivilege(domain)) {
				throw AuthExceptionType.AUTH_DOMAIN_NOT_AUTHORIZED.createException(domain.getName());
			}
		}

		@Override
		public boolean hasCreatePrivilege(ITable table) {
			return table.getMode().cardsAllowed() && hasWritePrivilege(table);
		}

		@Override
		public boolean hasCreatePrivilege(IDomain domain) {
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

	private final AuthenticatedUser user;
	private final FactoryManager factoryManager;

	public AuthenticatedUserWrapper(final AuthenticatedUser user) {
		this.user = user;
		this.factoryManager = new FactoryManager(this);
	}

	@Override
	public boolean belongsTo(String groupName) {
		return getCMGroupByName(groupName) != null;
	}

	@Override
	public boolean canChangePassword() {
		return user.canChangePassword();
	}

	@Override
	public boolean allowsPasswordLogin() {
		return user.canChangePassword(); // Not really
	}

	@Override
	public void changePassword(String oldPassword, String newPassword) {
		user.changePassword(oldPassword, newPassword);
	}

	@Override
	public DomainFactory domains() {
		return factoryManager.getDomainFactory();
	}

	@Override
	public Group getDefaultGroup() {
		return getGroupByName(user.getPreferredGroupName());
	}

	@Override
	public Collection<Group> getGroups() {
		final Collection<CMGroup> cmGroups = user.getGroups();
		final List<Group> groups = new ArrayList<Group>(cmGroups.size());
		for (final CMGroup cmg : cmGroups) {
			final Group g = groupFromCMGroup(cmg);
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
				return user.getDescription();
			}

			@Override
			public String getEncryptedPassword() {
				throw new UnsupportedOperationException("Should never be called!");
			}

			@Override
			public int getId() {
				final Long id = user.getId();
				if (id == null) {
					return 0;
				} else {
					return id.intValue();
				}
			}

			@Override
			public String getName() {
				return user.getName();
			}
		};
	}

	@Override
	public UserType getUserType() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public String getUsername() {
		return user.getName();
	}

	@Override
	public Group getWFStartGroup() {
		return this.getDefaultGroup();
	}

	@Override
	public boolean hasDefaultGroup() {
		return (user.getDefaultGroupName() != null);
	}

	@Override
	public boolean isGuest() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public PrivilegeManager privileges() {
		return new PrivilegeManagerWrap();
	}

	@Override
	public ProcessTypeFactory processTypes() {
		return factoryManager.getProcessTypeFactory();
	}

	@Override
	public RelationFactory relations() {
		return factoryManager.getRelationFactory();
	}

	@Override
	public ITableFactory tables() {
		return factoryManager.getTableFactory();
	}

	/*
	 * Utils and conversion
	 */

	private Group getGroupByName(final String groupName) {
		return groupFromCMGroup(getCMGroupByName(groupName));
	}

	private Group groupFromCMGroup(final CMGroup cmg) {
		if (cmg == null) {
			return null;
		}
		final Group g = new Group() {

			@Override
			public String getDescription() {
				return cmg.getDescription();
			}

			@Override
			public UIConfiguration getUIConfiguration() {
				final UIConfiguration uiConfiguration = new UIConfiguration();
				uiConfiguration.setDisabledModules(disabledModules());
				return uiConfiguration;
			}

			private String[] disabledModules() {
				final Set<String> dm = cmg.getDisabledModules();
				return dm.toArray(new String[dm.size()]);
			}

			@Override
			public int getId() {
				final Long id = user.getId();
				if (id == null) {
					return 0;
				} else {
					return id.intValue();
				}
			}

			@Override
			public String getName() {
				return cmg.getName();
			}

			@Override
			public ITable getStartingClass() {
				final Long scid = cmg.getStartingClassId();
				if (scid != null) {
					return tables().get(scid.intValue());
				}
				return null;
			}

			@Override
			public boolean isAdmin() {
				for (PrivilegeSet.PrivilegePair pp : cmg.getAllPrivileges()) {
					pp.privilege.implies(DefaultPrivileges.GOD);
				}
				return false;
			}

			@Override
			public boolean isDefault() {
				throw new UnsupportedOperationException("Not implemented yet");
			}
		};
		return g;
	}

	private CMGroup getCMGroupByName(final String groupName) {
		if (groupName == null) {
			return null;
		}
		for (final CMGroup group : user.getGroups()) {
			if (groupName.equals(group.getName())) {
				return group;
			}
		}
		return null;
	}
}
