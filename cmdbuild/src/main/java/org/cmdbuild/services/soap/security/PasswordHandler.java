package org.cmdbuild.services.soap.security;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.ws.security.WSPasswordCallback;
import org.cmdbuild.auth.AuthenticatedUser;
import org.cmdbuild.auth.DefaultAuthenticationService;
import org.cmdbuild.auth.DefaultAuthenticationService.PasswordCallback;
import org.cmdbuild.auth.Login;
import org.cmdbuild.config.AuthProperties;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * PasswordHandler class is used only with WSSecurity. This class verifies if
 * username and password in SOAP Message Header match with stored CMDBuild
 * credentials
 */
public class PasswordHandler implements CallbackHandler {

	private static class WSAuthenticationString {

		private final Login login;

		private WSAuthenticationString(final String username) {
			login = Login.newInstance(username);
		}

		private Login getAuthenticationLogin() {
			return login;
		}

		// TODO
		private Login getImpersonationLogin() {
			return null;
		}

		// TODO
		private boolean shouldImpersonate() {
			return false;
		}
	}

	@Autowired
	DefaultAuthenticationService as;

	@Override
	public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		for (final Callback callback : callbacks) {
			if (callback instanceof WSPasswordCallback) {
				final AuthenticatedUser user;
				final WSPasswordCallback pwcb = (WSPasswordCallback) callback;
				final WSAuthenticationString wsAuthString = new WSAuthenticationString(pwcb.getIdentifier());
				user = login(pwcb, wsAuthString.getAuthenticationLogin());
				if (user == null) {
					throw new UnsupportedCallbackException(pwcb);
				}
				if (wsAuthString.shouldImpersonate()) {
					as.impersonate(wsAuthString.getImpersonationLogin());
				}
			}
		}
	}

	private AuthenticatedUser login(final WSPasswordCallback pwcb, final Login login)
			throws UnsupportedCallbackException {
		final AuthenticatedUser user;
		if (pwcb.getUsage() == WSPasswordCallback.USERNAME_TOKEN) {
			user = as.authenticate(login, new PasswordCallback() {
				@Override
				public void setPassword(final String password) {
					pwcb.setPassword(password);
				}
			});
		} else {
			if (AuthProperties.getInstance().getForceWSPasswordDigest()) {
				throw new UnsupportedCallbackException(pwcb, "Unsupported authentication method");
			}
			user = as.authenticate(login, pwcb.getPassword());
		}
		return user;
	}
}
