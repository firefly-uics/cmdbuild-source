package org.cmdbuild.services.auth;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.ws.security.WSPasswordCallback;
import org.cmdbuild.config.AuthProperties;
import org.cmdbuild.elements.wrappers.UserCard;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.RedirectException;
import org.cmdbuild.logger.Log;

public class AuthenticationService implements Authenticator {

	private static final Logger logger = Log.AUTH;

	private static final String AUTH_METHODS_PACKAGE = "org.cmdbuild.services.auth";
	private static final List<Authenticator> authMethods = new ArrayList<Authenticator>();

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

	@Override
	public UserContext headerAuth(final HttpServletRequest request) throws RedirectException {
		UserContext userCtx = null;
		for (final Authenticator method : authMethods) {
			userCtx = method.headerAuth(request);
			if (userCtx != null) {
				break;
			}
		}
		return userCtx;
	}

	@Override
	public UserContext jsonRpcAuth(final String username, final String unencryptedPassword) {
		UserContext userCtx = null;
		for (final Authenticator method : authMethods) {
			try {
				userCtx = method.jsonRpcAuth(username, unencryptedPassword);
			} catch (final Exception e) {
				assert userCtx == null;
			}
			if (userCtx != null) {
				userCtx.setAuthenticator(method);
				break;
			}
		}
		return userCtx;
	}

	@Override
	public boolean wsAuth(final WSPasswordCallback pwcb) {
		for (final Authenticator method : authMethods) {
			if (method.wsAuth(pwcb)) {
				return true;
			}
		}
		return false;
	}

	public UserContext getWSUserContext(final String authData) {
		logger.debug(String.format("getting user context for '%s'", authData));
		UserContext userCtx = null;
		if (authData != null) {
			final AuthInfo authInfo = new AuthInfo(authData);
			final String username = authInfo.getUsername();
			User user = null;
			if (!authInfo.isPrivilegedServiceUser()) {
				try {
					user = UserCard.getUser(username);
					userCtx = new UserContext(user);
				} catch (final AuthException e) {
					logger.warn("could not get user", e);
				}
			}
			if (user == null) {
				user = UserCard.getUser(authInfo);
				userCtx = new UserContext(user, username);
			}
			authInfo.checkOrSetDefaultGroup(userCtx);
		}
		return userCtx;
	}

	@Override
	public void changePassword(final String username, final String oldPassword, final String newPassword) {
		for (final Authenticator method : authMethods) {
			method.changePassword(username, oldPassword, newPassword);
		}
	}

	@Override
	public boolean canChangePassword() {
		for (final Authenticator method : authMethods) {
			if (method.canChangePassword()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean allowsPasswordLogin() {
		for (final Authenticator method : authMethods) {
			if (method.allowsPasswordLogin()) {
				return true;
			}
		}
		return false;
	}

}
