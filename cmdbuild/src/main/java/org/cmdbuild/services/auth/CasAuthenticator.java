package org.cmdbuild.services.auth;

import javax.servlet.http.HttpServletRequest;

import org.apache.ws.security.WSPasswordCallback;
import org.cmdbuild.auth.ClientRequestAuthenticator.Response;
import org.cmdbuild.auth.Login;
import org.cmdbuild.config.AuthProperties;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.exception.RedirectException;
import org.cmdbuild.logger.Log;

public class CasAuthenticator implements Authenticator {

	private org.cmdbuild.auth.CasAuthenticator newImplementation;

	public CasAuthenticator() {
		if (!AuthProperties.getInstance().isCasConfigured()) {
			throw AuthExceptionType.AUTH_NOT_CONFIGURED.createException();
		}
		this.newImplementation = new org.cmdbuild.auth.CasAuthenticator(AuthProperties.getInstance());
	}

	@Override
	public UserContext headerAuth(final HttpServletRequest request) throws RedirectException {
		final Response response = newImplementation.authenticate(new ClientRequestWrapper(request));

		if (response != null) {
			if (response.getRedirectUrl() != null) {
				throw new RedirectException(response.getRedirectUrl());
			} else {
				final Login login = response.getLogin();
				return loginWithUsername(login.getValue());
			}
		}
		return null;
	}

	private UserContext loginWithUsername(final String username) {
		try {
			return new AuthInfo(username).systemAuth();
		} catch (Throwable e) {
			Log.AUTH.warn(String.format("CAS user %s has no valid CMDBuild", username));
			return null;
		}
	}

	@Override
	public UserContext jsonRpcAuth(String username, String unencryptedPassword) {
		return null;
	}

	@Override
	public boolean wsAuth(WSPasswordCallback pwcb) {
		return false;
	}

	@Override
	public boolean canChangePassword() {
		return false;
	}

	@Override
	public void changePassword(String username, String oldPassword, String newPassword) {
		throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
	}

	@Override
	public boolean allowsPasswordLogin() {
		return false;
	}
}
