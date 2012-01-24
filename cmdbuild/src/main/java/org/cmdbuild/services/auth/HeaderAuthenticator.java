package org.cmdbuild.services.auth;

import javax.servlet.http.HttpServletRequest;

import org.apache.ws.security.WSPasswordCallback;
import org.cmdbuild.auth.ClientRequestAuthenticator.Response;
import org.cmdbuild.auth.Login;
import org.cmdbuild.config.AuthProperties;
import org.cmdbuild.exception.AuthException.AuthExceptionType;

public class HeaderAuthenticator implements Authenticator {

	private org.cmdbuild.auth.HeaderAuthenticator newImplementation;

	public HeaderAuthenticator() {
		if (!AuthProperties.getInstance().isHeaderConfigured()) {
			throw AuthExceptionType.AUTH_NOT_CONFIGURED.createException();
		}
		newImplementation = new org.cmdbuild.auth.HeaderAuthenticator(AuthProperties.getInstance());
	}

	@Override
	public UserContext headerAuth(final HttpServletRequest request) {
		final Response response = newImplementation.authenticate(new ClientRequestWrapper(request));
		try {
			if (response != null) {
				final Login login = response.getLogin();
				if (login != null) {
					return new AuthInfo(login.getValue()).systemAuth();
				}
			}
		} catch (final Throwable e) {
		}
		return null;
	}

	@Override
	public UserContext jsonRpcAuth(final String username, final String unencryptedPassword) {
		return null;
	}

	@Override
	public boolean wsAuth(final WSPasswordCallback pwcb) {
		return false;
	}

	@Override
	public boolean canChangePassword() {
		return false;
	}

	@Override
	public void changePassword(final String username, final String oldPassword, final String newPassword) {
		throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
	}

	@Override
	public boolean allowsPasswordLogin() {
		return false;
	}
}
