package org.cmdbuild.auth.user;

public class AnonymousUser extends AuthenticatedUser {

	public static final AuthenticatedUser ANONYMOUS_USER = new AnonymousUser();

	private AnonymousUser() {
	}

	@Override
	public void changePassword(String oldPassword, String newPassword) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean canChangePassword() {
		return false;
	}
}
