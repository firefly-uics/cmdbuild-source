package org.cmdbuild.auth.user;

import java.util.Collections;
import java.util.Set;

import org.cmdbuild.auth.PasswordAuthenticator.PasswordChanger;
import org.cmdbuild.auth.acl.CMGroup;

public class AnonymousUser implements AuthenticatedUser {

	@Override
	public Long getId() {
		return null;
	}

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
		return Collections.emptySet();
	}

	@Override
	public String getDefaultGroupName() {
		return null;
	}

	@Override
	public boolean isAnonymous() {
		return true;
	}

	@Override
	public void setPasswordChanger(final PasswordChanger passwordChanger) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean changePassword(final String oldPassword, final String newPassword) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean canChangePassword() {
		return false;
	}

}
