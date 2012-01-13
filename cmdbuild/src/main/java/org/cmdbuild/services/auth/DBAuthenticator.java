package org.cmdbuild.services.auth;

import javax.servlet.http.HttpServletRequest;

import org.apache.ws.security.WSPasswordCallback;
import org.cmdbuild.auth.password.NaivePasswordHandler;
import org.cmdbuild.auth.password.PasswordHandler;
import org.cmdbuild.elements.wrappers.UserCard;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.services.WorkflowService;

public class DBAuthenticator implements Authenticator {

	@Override
	public UserContext headerAuth(final HttpServletRequest request) {
		return null;
	}

	@Override
	public UserContext jsonRpcAuth(final String username, final String unencryptedPassword) {
		final UserContext userCtx = new AuthInfo(username).systemAuth();
		if (passwordMatch(userCtx.getUser(), unencryptedPassword)) {
			return userCtx;
		} else {
			return null;
		}
	}

	private boolean passwordMatch(final User user, final String unencryptedPassword) {
		final PasswordHandler se = new NaivePasswordHandler();
		final String encpass = se.encrypt(unencryptedPassword);
		return user.getEncryptedPassword().equals(encpass);
	}

	@Override
	public boolean wsAuth(final WSPasswordCallback pwcb) {
		final AuthInfo authInfo = new AuthInfo(pwcb.getIdentifier());
		final String unencryptedPassword = getUnencryptedPassword(authInfo);
		if (isDigested(pwcb)) {
			pwcb.setPassword(unencryptedPassword);
			// it should stop the authentication chain because it does not make
			// sense to have the password set multiple times (it is done here
			// only, indeed)
			return true;
		} else {
			return checkPassword(pwcb, unencryptedPassword);
		}
	}

	private String getUnencryptedPassword(final AuthInfo authInfo) {
		if (authInfo.isSharkUser()) {
			return WorkflowService.getInstance().getSharkWSPassword();
		}
		final User user = getUser(authInfo);
		final PasswordHandler enc = new NaivePasswordHandler();
		final String encryptedPassword = user.getEncryptedPassword();
		final String unencryptedPassword = enc.decrypt(encryptedPassword);
		return unencryptedPassword;
	}

	private User getUser(final AuthInfo authInfo) {
		try {
			return UserCard.getUser(authInfo.getUsernameForAuthentication());
		} catch (final CMDBException e) {
			throw AuthExceptionType.AUTH_LOGIN_WRONG.createException();
		}
	}

	private boolean checkPassword(final WSPasswordCallback pwcb, final String unencryptedPassword) {
		final String messagePassword = pwcb.getPassword();
		return (messagePassword.equals(unencryptedPassword));
	}

	private boolean isDigested(final WSPasswordCallback pwcb) {
		return (pwcb.getPassword() == null);
	}

	@Override
	public void changePassword(final String username, final String oldPassword, final String newPassword) {
		if (jsonRpcAuth(username, oldPassword) != null) {
			final UserCard user = UserCard.getUserCard(username);
			user.setUnencryptedPassword(newPassword);
			user.save();
		} else {
			throw AuthExceptionType.AUTH_WRONG_PASSWORD.createException();
		}
	}

	@Override
	public boolean canChangePassword() {
		return true;
	}

	@Override
	public boolean allowsPasswordLogin() {
		return true;
	}
}
