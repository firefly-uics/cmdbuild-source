package org.cmdbuild.auth.user;

import org.cmdbuild.auth.PasswordAuthenticator.PasswordChanger;

public class ForwardingAuthenticatedUser extends ForwardingUser implements AuthenticatedUser {

	private final AuthenticatedUser inner;

	public ForwardingAuthenticatedUser(final AuthenticatedUser authenticatedUser) {
		super(authenticatedUser);
		this.inner = authenticatedUser;
	}

	@Override
	public boolean isAnonymous() {
		return inner.isAnonymous();
	}

	@Override
	public void setPasswordChanger(final PasswordChanger passwordChanger) {
		inner.setPasswordChanger(passwordChanger);
	}

	@Override
	public boolean changePassword(final String oldPassword, final String newPassword) {
		return inner.changePassword(oldPassword, newPassword);
	}

	@Override
	public boolean canChangePassword() {
		return inner.canChangePassword();
	}

}
