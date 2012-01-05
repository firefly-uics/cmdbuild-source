package org.cmdbuild.auth.user;

public abstract class AuthenticatedUser implements CMUser {

	public abstract void changePassword(String oldPassword, String newPassword);
	public abstract boolean canChangePassword();

	public static AuthenticatedUser newInstance(final CMUser user) {
		if (user == null) {
			return AnonymousUser.ANONYMOUS_USER;
		} else {
			return new AuthenticatedUserWrapper(user);
		}
	}
}
