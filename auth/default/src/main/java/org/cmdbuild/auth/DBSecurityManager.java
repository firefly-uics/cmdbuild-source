package org.cmdbuild.auth;

import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.auth.PasswordAuthenticator.PasswordChanger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBSecurityManager implements UserFetcher, PasswordAuthenticator, PasswordChanger {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public boolean checkPassword(Login login, String password) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String fetchUnencryptedPassword(Login login) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void changePassword(Login login, String oldPassword, String newPassword) {
		throw new UnsupportedOperationException("Not supported yet.");
		// if (checkPassword(login, oldPassword)) ...
	}

	@Override
	public CMUser fetchUser(Login login) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/*
	 * Methods to manage users, group and privileges go here
	 */
}
