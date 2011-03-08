package org.cmdbuild.services.soap.security;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.ws.security.WSPasswordCallback;
import org.cmdbuild.config.AuthProperties;
import org.cmdbuild.services.auth.AuthenticationService;

/**
 * PasswordHandler class is used only with WSSecurity. This class verifies if
 * username and password in SOAP Message Header match with stored CMDBuild
 * credentials
 */
public class PasswordHandler implements CallbackHandler {

	public void handle(Callback[] callbacks) throws IOException,
			UnsupportedCallbackException {

		for (int i = 0; i < callbacks.length; i++) {
			if (callbacks[i] instanceof WSPasswordCallback) {
				WSPasswordCallback pwcb = (WSPasswordCallback) callbacks[i];
				checkPasswordPolicy(pwcb);
				AuthenticationService authenticator = new AuthenticationService();
				if (!authenticator.wsAuth(pwcb)) {
					throw new UnsupportedCallbackException(pwcb);
				}
			}
		}
	}

	private void checkPasswordPolicy(WSPasswordCallback pwcb) throws UnsupportedCallbackException {
		if (AuthProperties.getInstance().getForceWSPasswordDigest()) {
			if (pwcb.getUsage() != WSPasswordCallback.USERNAME_TOKEN) {
				throw new UnsupportedCallbackException(pwcb, "Unrecognized Callback");
			}
		}
	}
}
