package org.cmdbuild.services.auth;

import javax.servlet.http.HttpServletRequest;

import org.apache.ws.security.WSPasswordCallback;
import org.cmdbuild.config.AuthProperties;
import org.cmdbuild.exception.AuthException.AuthExceptionType;

public class HeaderAuthenticator implements Authenticator {

	public HeaderAuthenticator() {
		if (!AuthProperties.getInstance().isHeaderConfigured()) {
			throw AuthExceptionType.AUTH_NOT_CONFIGURED.createException();
		}
	}

	@Override
	public UserContext headerAuth(final HttpServletRequest request) {
		final String headerAttribute = AuthProperties.getInstance().getHeaderAttributeName();
		final String username = request.getHeader(headerAttribute);
		try {
			if (username != null) {
				return new AuthInfo(username).systemAuth();
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
