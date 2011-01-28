package org.cmdbuild.services.auth;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.handler.MessageContext;

import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;
import org.cmdbuild.config.AuthProperties;
import org.cmdbuild.elements.wrappers.UserCard;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logger.Log;

public class AuthenticationService implements Authenticator {

	static private String AUTH_METHODS_PACKAGE = "org.cmdbuild.services.auth";
	static private List<Authenticator> authMethods = new ArrayList<Authenticator>();

	public void loadAuthMethods() {
		for (String name : AuthProperties.getInstance().getAuthMethodNames()) {
			try {
				Log.AUTH.info("Adding authentication method " + name);
				Class<?> cls = Class.forName(AUTH_METHODS_PACKAGE+"."+name);
				Constructor<?> ct = cls.getConstructor();
				Authenticator authenticator = (Authenticator) ct.newInstance();
				authMethods.add(authenticator);
			} catch (AuthException e) {
				Log.AUTH.error(name + " not configured: skipping");
			} catch (Exception e) {
				Log.AUTH.warn("Could not import authentication method " + name);
				Log.AUTH.debug(e);
			}
		}
	}

	public UserContext headerAuth(HttpServletRequest request) {
		UserContext userCtx = null;
		for (Authenticator method : authMethods) {
			userCtx = method.headerAuth(request);
			if (userCtx != null) {
				break;
			}
		}
		return userCtx;
	}

	public UserContext jsonRpcAuth(String username, String unencryptedPassword) {
		UserContext userCtx = null;
		for (Authenticator method : authMethods) {
			userCtx = method.jsonRpcAuth(username, unencryptedPassword);
			if (userCtx != null) {
				userCtx.setAuthenticator(method);
				break;
			}
		}
		return userCtx;
	}

	public boolean wsAuth(WSPasswordCallback pwcb) {
		for (Authenticator method : authMethods) {
			if (method.wsAuth(pwcb)) {
				return true;
			}
		}
		return false;
	}

	public UserContext getWSUserContext(String authData) {
		UserContext userCtx = null;
		if (authData != null) {
			User user;
			String username = AuthenticationUtils.getUsername(authData);
			
			try {
				user = UserCard.findByUserName(username);
				userCtx = new UserContext(user);
			} catch (NotFoundException e) {
				String authusername = AuthenticationUtils.getUsernameForAuthentication(authData);
				user = UserCard.findByUserName(authusername);
				userCtx = new UserContext(user, username);
			}
			AuthenticationUtils.checkOrSetDefaultGroup(authData, userCtx);
		}
		return userCtx;
	}

	public void changePassword(String username, String oldPassword, String newPassword) {
		for (Authenticator method : authMethods) {
			method.changePassword(username, oldPassword, newPassword);
		}
	}

	public boolean canChangePassword() {
		for (Authenticator method : authMethods) {
			if (method.canChangePassword()){
				return true;
			}
		}
		return false;
	}
}
