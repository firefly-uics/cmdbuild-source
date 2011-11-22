package org.cmdbuild.services.auth;

import javax.servlet.http.HttpServletRequest;

import org.apache.ws.security.WSPasswordCallback;
import org.cmdbuild.auth.Login;
import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.services.WorkflowService;

public class DBAuthenticator implements Authenticator {

	private org.cmdbuild.auth.LegacyDBAuthenticator newImplementation;

	private synchronized org.cmdbuild.auth.LegacyDBAuthenticator getNewImplementation() {
		if (newImplementation == null) {
			final DBDriver driver = TemporaryObjectsBeforeSpringDI.getDriver();
			final CMDataView view = new DBDataView(driver);
			newImplementation = new org.cmdbuild.auth.LegacyDBAuthenticator(view);
		}
		return newImplementation;
	}

	@Override
	public UserContext headerAuth(final HttpServletRequest request) {
		return null;
	}

	@Override
	public UserContext jsonRpcAuth(final String username, final String unencryptedPassword) {
		final UserContext userCtx = new AuthInfo(username).systemAuth();
		final Login login = Login.newInstance(username);
		if (getNewImplementation().checkPassword(login, unencryptedPassword)) {
			return userCtx;
		} else {
			return null;
		}
	}

	@Override
	public boolean wsAuth(final WSPasswordCallback pwcb) {
		final AuthInfo authInfo = new AuthInfo(pwcb.getIdentifier());
		final Login login = Login.newInstance(authInfo.getUsernameForAuthentication());
		final String unencryptedPassword = pwcb.getPassword();
		if (unencryptedPassword != null) {
			if (authInfo.isSharkUser()) {
				return WorkflowService.getInstance().getSharkWSPassword().equals(unencryptedPassword);
			} else {
				return getNewImplementation().checkPassword(login, unencryptedPassword);
			}
		} else {
			// digested password
			final String dbUnencryptedPassword;
			if (authInfo.isSharkUser()) {
				dbUnencryptedPassword = WorkflowService.getInstance().getSharkWSPassword();
			} else {
				dbUnencryptedPassword = getNewImplementation().fetchUnencryptedPassword(login);
			}
			pwcb.setPassword(dbUnencryptedPassword);
			// it should stop the authentication chain because it does not make
			// sense to have the password set multiple times (it is done here
			// only, indeed)
			return true;
		}
	}

	@Override
	public void changePassword(final String username, final String oldPassword, final String newPassword) {
		final Login login = Login.newInstance(username);
		try {
			getNewImplementation().getPasswordChanger(login).changePassword(oldPassword, newPassword);
		} catch (IllegalArgumentException e) {
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
