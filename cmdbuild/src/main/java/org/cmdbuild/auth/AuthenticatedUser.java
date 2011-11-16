package org.cmdbuild.auth;

import org.cmdbuild.auth.Login;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.auth.user.OperationUser;


public abstract class AuthenticatedUser implements OperationUser {

	public static AuthenticatedUser newInstance(final CMUser user) {
		if (user == null) {
			return AnonymousUser.ANONYMOUS_USER;
		} else {
			return new AuthenticatedUserWrapper(user);
		}
	}

	public abstract void changePassword(String oldPassword, String newPassword);
	public abstract boolean canChangePassword();

	public abstract void selectGroup(String name);
	public abstract void impersonate(Login login);

}
