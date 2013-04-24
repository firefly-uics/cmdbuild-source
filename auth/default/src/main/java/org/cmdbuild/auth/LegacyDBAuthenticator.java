package org.cmdbuild.auth;

import java.util.NoSuchElementException;

import org.apache.commons.lang.Validate;
import org.cmdbuild.common.digest.Base64Digester;
import org.cmdbuild.common.digest.Digester;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.view.CMDataView;

/**
 * Checks password stored in the DAO layer
 */
public class LegacyDBAuthenticator extends LegacyDBUserFetcher implements PasswordAuthenticator {

	protected final Digester digester;

	public LegacyDBAuthenticator(final CMDataView view) {
		this(view, new Base64Digester());
	}

	/*
	 * Used by tests
	 */
	public LegacyDBAuthenticator(final CMDataView view, final Base64Digester digester) {
		super(view);
		Validate.notNull(digester);
		this.digester = digester;
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
		final String encryptedPassword = digester.encrypt(password);
		final String dbEncryptedPassword = fetchEncryptedPassword(login);
		return encryptedPassword.equals(dbEncryptedPassword);
	}

	@Override
	public String fetchUnencryptedPassword(final Login login) {
		final String dbEncryptedPassword = fetchEncryptedPassword(login);
		return digester.decrypt(dbEncryptedPassword);
	}

	private String fetchEncryptedPassword(final Login login) {
		try {
			final CMCard userCard = fetchUserCard(login);
			return userCard.get(userPasswordAttribute()).toString();
		} catch (final NoSuchElementException e) {
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

	private boolean changePassword(final Login login, final String oldPassword, final String newPassword) {
		if (checkPassword(login, oldPassword)) {
			try {
				final String newEncryptedPassword = digester.encrypt(newPassword);
				final CMCard userCard = fetchUserCard(login);
				view.update(userCard).set(userPasswordAttribute(), newEncryptedPassword).save();
				return true;
			} catch (final NoSuchElementException e) {
				// let it return false
			}
		}
		return false;
	}
}
