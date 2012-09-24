package org.cmdbuild.auth.user;

import org.apache.commons.lang.Validate;

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
	public void changePassword(String oldPassword, String newPassword) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public boolean canChangePassword() {
		throw new UnsupportedOperationException("Not implemented");
		// if the authenticator is a PasswordChanger
	}
}
