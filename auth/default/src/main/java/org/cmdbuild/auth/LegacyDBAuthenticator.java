package org.cmdbuild.auth;

import java.util.NoSuchElementException;

import org.apache.commons.lang.Validate;
import org.cmdbuild.auth.password.NaivePasswordHandler;
import org.cmdbuild.auth.password.PasswordHandler;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.view.CMDataView;

/**
 * Checks password stored in the DAO layer
 */
public class LegacyDBAuthenticator extends LegacyDBUserFetcher implements PasswordAuthenticator {

	protected final PasswordHandler passwordHandler;

	public LegacyDBAuthenticator(final CMDataView view) {
		this(view, new NaivePasswordHandler());
	}

	/*
	 * Used by tests
	 */
	public LegacyDBAuthenticator(final CMDataView view, final PasswordHandler passwordHandler) {
		super(view);
		Validate.notNull(passwordHandler);
		this.passwordHandler = passwordHandler;
	}

	@Override
	public String getName() {
		return "DBAuthenticator";
	}

	@Override
	public boolean checkPassword(final Login login, final String password) {
		if (password == null) {
			return false;
		}
		final String encryptedPassword = passwordHandler.encrypt(password);
		final String dbEncryptedPassword = fetchEncryptedPassword(login);
		return encryptedPassword.equals(dbEncryptedPassword);
	}

	@Override
	public String fetchUnencryptedPassword(final Login login) {
		final String dbEncryptedPassword = fetchEncryptedPassword(login);
		return passwordHandler.decrypt(dbEncryptedPassword);
	}

	private String fetchEncryptedPassword(final Login login) {
		try {
			final CMCard userCard = fetchUserCard(login);
			return userCard.get(userPasswordAttribute()).toString();
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	@Override
	public PasswordChanger getPasswordChanger(final Login login) {
		return new PasswordChanger() {

			@Override
			public boolean changePassword(final String oldPassword, final String newPassword) {
				return LegacyDBAuthenticator.this.changePassword(login, oldPassword, newPassword);
			}
		};
	}

	private boolean changePassword(final Login login, final String oldPassword, String newPassword) {
		if (checkPassword(login, oldPassword)) {
			try {
				final String newEncryptedPassword = passwordHandler.encrypt(newPassword);
				final CMCard userCard = fetchUserCard(login);
				view.modifyCard(userCard)
						.set(userPasswordAttribute(), newEncryptedPassword)
						.save();
				return true;
			} catch (NoSuchElementException e) {
				// let it return false
			}
		}
		return false;
	}
}
