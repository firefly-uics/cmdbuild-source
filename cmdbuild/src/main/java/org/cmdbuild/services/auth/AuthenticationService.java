package org.cmdbuild.services.auth;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.ws.security.WSPasswordCallback;
import org.cmdbuild.config.AuthProperties;
import org.cmdbuild.elements.wrappers.UserCard;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logger.Log;

public class AuthenticationService implements Authenticator {

	static private String AUTH_METHODS_PACKAGE = "org.cmdbuild.services.auth";
	static private List<Authenticator> authMethods = new ArrayList<Authenticator>();

	public void loadAuthMethods() {
		for (final String name : AuthProperties.getInstance().getAuthMethodNames()) {
			try {
				Log.AUTH.info("Adding authentication method " + name);
				final Class<?> cls = Class.forName(AUTH_METHODS_PACKAGE + "." + name);
				final Constructor<?> ct = cls.getConstructor();
				final Authenticator authenticator = (Authenticator) ct.newInstance();
				authMethods.add(authenticator);
			} catch (final AuthException e) {
				Log.AUTH.error(name + " not configured: skipping");
			} catch (final Exception e) {
				Log.AUTH.warn("Could not import authentication method " + name);
				Log.AUTH.debug(e);
			}
		}
	}

	public UserContext headerAuth(final HttpServletRequest request) {
		UserContext userCtx = null;
		for (final Authenticator method : authMethods) {
			userCtx = method.headerAuth(request);
			if (userCtx != null) {
				break;
			}
		}
		return userCtx;
	}

	public UserContext jsonRpcAuth(final String username, final String unencryptedPassword) {
		UserContext userCtx = null;
		for (final Authenticator method : authMethods) {
			userCtx = method.jsonRpcAuth(username, unencryptedPassword);
			if (userCtx != null) {
				userCtx.setAuthenticator(method);
				break;
			}
		}
		return userCtx;
	}

	public boolean wsAuth(final WSPasswordCallback pwcb) {
		for (final Authenticator method : authMethods) {
			if (method.wsAuth(pwcb)) {
				return true;
			}
		}
		return false;
	}

	public UserContext getWSUserContext(final String authData) {
		UserContext userCtx = null;
		if (authData != null) {
			final AuthInfo authInfo = new AuthInfo(authData);
			User user;
			try {
				user = UserCard.getUser(authInfo.getUsername());
				userCtx = new UserContext(user);
			} catch (final NotFoundException e) {
				final String authusername = authInfo.getUsernameForAuthentication();
				user = UserCard.getUser(authusername);
				userCtx = new UserContext(user, authInfo.getUsername());
			}
			authInfo.checkOrSetDefaultGroup(userCtx);
		}
		return userCtx;
	}

	public void changePassword(final String username, final String oldPassword, final String newPassword) {
		for (final Authenticator method : authMethods) {
			method.changePassword(username, oldPassword, newPassword);
		}
	}

	public boolean canChangePassword() {
		for (final Authenticator method : authMethods) {
			if (method.canChangePassword()) {
				return true;
			}
		}
		return false;
	}

}
