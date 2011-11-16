package org.cmdbuild.auth;

import org.apache.commons.lang.Validate;
import org.cmdbuild.auth.Login;
import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.CMPrivilegedObject;
import org.cmdbuild.auth.user.CMUser;

public class AuthenticatedUserWrapper extends AuthenticatedUser {

	final CMUser inner;

	AuthenticatedUserWrapper(final CMUser user) {
		Validate.notNull(user);
		if (user instanceof AuthenticatedUserWrapper) {
			this.inner = ((AuthenticatedUserWrapper)user).inner;
		} else {
			this.inner = user;
		}
	}

	@Override
	public String getUsername() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public boolean hasReadAccess(CMPrivilegedObject privilegedObject) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public boolean hasWriteAccess(CMPrivilegedObject privilegedObject) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public boolean hasAdministratorPrivileges() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean hasDatabaseDesignerPrivileges() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean hasPrivilege(CMPrivilege privilege) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Iterable<PrivilegePair> getAllPrivileges() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean hasPrivilege(CMPrivilege requested, CMPrivilegedObject privilegedObject) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void changePassword(String oldPassword, String newPassword) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public boolean canChangePassword() {
		throw new UnsupportedOperationException("Not implemented");
		// if the authenticator is a PasswordChanger
	}

	@Override
	public void selectGroup(String name) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void impersonate(Login login) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
