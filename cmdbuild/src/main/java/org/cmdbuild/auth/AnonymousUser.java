package org.cmdbuild.auth;

import java.util.ArrayList;
import org.cmdbuild.auth.Login;
import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.CMPrivilegedObject;

public class AnonymousUser extends AuthenticatedUser {

	public static final AuthenticatedUser ANONYMOUS_USER = new AnonymousUser();

	private AnonymousUser() {
	}

	@Override
	public void changePassword(final String oldPassword, final String newPassword) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean canChangePassword() {
		return false;
	}

	@Override
	public String getUsername() {
		return "anonymous";
	}

	@Override
	public boolean hasReadAccess(final CMPrivilegedObject privilegedObject) {
		return false;
	}

	@Override
	public boolean hasWriteAccess(final CMPrivilegedObject privilegedObject) {
		return false;
	}

	@Override
	public boolean hasAdministratorPrivileges() {
		return false;
	}

	@Override
	public boolean hasDatabaseDesignerPrivileges() {
		return false;
	}

	@Override
	public boolean hasPrivilege(CMPrivilege privilege) {
		return false;
	}

	@Override
	public boolean hasPrivilege(CMPrivilege requested, CMPrivilegedObject privilegedObject) {
		return false;
	}

	@Override
	public Iterable<PrivilegePair> getAllPrivileges() {
		return new ArrayList<PrivilegePair>();
	}

	@Override
	public void selectGroup(String name) {
		throw new UnsupportedOperationException("Unauthorized");
	}

	@Override
	public void impersonate(Login login) {
		throw new UnsupportedOperationException("Unauthorized");
	}
}
