package org.cmdbuild.auth;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import org.apache.commons.lang.Validate;
import org.cmdbuild.auth.PasswordAuthenticator.PasswordChanger;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.CMPrivilegedObject;
import org.cmdbuild.auth.acl.InspectableSecurityManager;
import org.cmdbuild.auth.acl.SimpleSecurityManager;
import org.cmdbuild.auth.user.CMUser;

public class AuthenticatedUserImpl implements AuthenticatedUser {

	private interface GroupFilter {
		boolean matches(CMGroup group);
	}

	private static class AnonymousUser extends AuthenticatedUserImpl {

		private AnonymousUser() {
			super(new CMUser() {

				@Override
				public String getName() {
					return "anonymous";
				}

				@Override
				public String getDescription() {
					return "Anonymous";
				}

				@Override
				public Set<CMGroup> getGroups() {
					return Collections.EMPTY_SET;
				}

				@Override
				public String getDefaultGroupName() {
					return null;
				}
			});
		}

		@Override
		public void setPasswordChanger(final PasswordChanger passwordChanger) {
			throw new UnsupportedOperationException();
		}
	}

	public static final AuthenticatedUserImpl ANONYMOUS_USER = new AnonymousUser();

	private final CMUser inner;
	private PasswordChanger passwordChanger;

	private CMGroup preferredGroup;
	private InspectableSecurityManager securityManager;

	public static AuthenticatedUserImpl newInstance(final CMUser user) {
		if (user == null) {
			return ANONYMOUS_USER;
		} else {
			return new AuthenticatedUserImpl(user);
		}
	}

	protected AuthenticatedUserImpl(final CMUser user) {
		Validate.notNull(user);
		if (user instanceof AuthenticatedUserImpl) {
			this.inner = ((AuthenticatedUserImpl)user).inner;
		} else {
			this.inner = user;
		}
		this.securityManager = mergeGroupPrivileges(inner, everyGroup());
		this.preferredGroup = guessPreferredGroup(inner);
	}

	private CMGroup guessPreferredGroup(final CMUser user) {
		CMGroup guessedGroup = getDefaultGroup(user);
		if (guessedGroup == null) {
			guessedGroup = getTheFirstAndOnlyGroup(user);
		}
		return guessedGroup;
	}

	private CMGroup getTheFirstAndOnlyGroup(final CMUser user) {
		CMGroup firstGroup = null;
		Iterator<CMGroup> groups = user.getGroups().iterator();
		if (groups.hasNext()) {
			firstGroup = groups.next();
			if (groups.hasNext()) {
				firstGroup = null;
			}
		}
		return firstGroup;
	}

	@Override
	public boolean isValid() {
		return getPreferredGroup() != null;
	}

	/*
	 * Password change
	 */

	public void setPasswordChanger(final PasswordChanger passwordChanger) {
		this.passwordChanger = passwordChanger;
	}

	@Override
	public final boolean changePassword(final String oldPassword, final String newPassword) {
		return canChangePassword() && passwordChanger.changePassword(oldPassword, newPassword);
	}

	@Override
	public final boolean canChangePassword() {
		return passwordChanger != null;
	}

	/*
	 * Preferred group
	 */

	@Override
	public final void selectGroup(final String groupName) {
		preferredGroup = getGroup(inner, groupName);
	}

	private CMGroup getGroup(final CMUser user, final String groupName) {
		for (final CMGroup g : user.getGroups()) {
			if (g.getName().equals(groupName)) {
				return g;
			}
		}
		return null;
	}

	private CMGroup getDefaultGroup(final CMUser user) {
		return getGroup(user, user.getDefaultGroupName());
	}


	// TODO: change after impersonate
	protected CMGroup getPreferredGroup() {
		return preferredGroup;
	}

	/*
	 * Privileges
	 */

	@Override
	public final void filterPrivileges(final String groupName) {
		this.securityManager = mergeGroupPrivileges(inner, filterName(groupName));
	}

	private InspectableSecurityManager mergeGroupPrivileges(final CMUser user, final GroupFilter filter) {
		SimpleSecurityManager.SimpleSecurityManagerBuilder builder = SimpleSecurityManager.newInstanceBuilder();
		for (final CMGroup g : user.getGroups()) {
			if (filter.matches(g)) {
				builder.withPrivileges(g.getAllPrivileges());
			}
		}
		return builder.build();
	}

	private GroupFilter filterName(final String name) {
		return new GroupFilter() {
			@Override
			public boolean matches(CMGroup group) {
				return group.getName().equals(name);
			}
		};
	}

	private GroupFilter everyGroup() {
		return new GroupFilter() {
			@Override
			public boolean matches(CMGroup group) {
				return true;
			}
		};
	}

	// TODO: change after impersonate
	protected InspectableSecurityManager getSecurityManager() {
		return securityManager;
	}

	/**
	 * Impersonates another user, if possible. This method should be called
	 * by the AuthenticationService.
	 *
	 * @param user user to impersonate
	 */
	@Override
	public void impersonate(CMUser user) {
		throw new IllegalArgumentException();
	}

	/*
	 * CMUser
	 */

	@Override
	public String getName() {
		return inner.getName();
	}

	@Override
	public String getDescription() {
		return inner.getDescription();
	}

	@Override
	public Set<CMGroup> getGroups() {
		return inner.getGroups();
	}

	@Override
	public String getDefaultGroupName() {
		return inner.getDefaultGroupName();
	}

	/*
	 * OperationUser
	 */

	@Override
	public String getOperationUsername() {
		return inner.getName();
	}

	@Override
	public String getPreferredGroupName() {
		if (preferredGroup == null) {
			throw new IllegalStateException("No group selected");
		}
		return preferredGroup.getName();
	}

	/*
	 * CMSecurityManager
	 */

	@Override
	public boolean hasReadAccess(CMPrivilegedObject privilegedObject) {
		return getSecurityManager().hasReadAccess(privilegedObject);
	}

	@Override
	public boolean hasWriteAccess(CMPrivilegedObject privilegedObject) {
		return getSecurityManager().hasWriteAccess(privilegedObject);
	}

	@Override
	public boolean hasAdministratorPrivileges() {
		return getSecurityManager().hasAdministratorPrivileges();
	}

	@Override
	public boolean hasDatabaseDesignerPrivileges() {
		return getSecurityManager().hasDatabaseDesignerPrivileges();
	}

	@Override
	public boolean hasPrivilege(CMPrivilege privilege) {
		return getSecurityManager().hasPrivilege(privilege);
	}

	@Override
	public boolean hasPrivilege(CMPrivilege requested, CMPrivilegedObject privilegedObject) {
		return getSecurityManager().hasPrivilege(requested, privilegedObject);
	}

}
