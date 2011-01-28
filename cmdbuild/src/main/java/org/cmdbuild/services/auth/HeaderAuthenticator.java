package org.cmdbuild.services.auth;

import javax.servlet.http.HttpServletRequest;

import org.apache.ws.security.WSPasswordCallback;
import org.cmdbuild.config.AuthProperties;
import org.cmdbuild.exception.AuthException.AuthExceptionType;

public class HeaderAuthenticator implements Authenticator{

	public HeaderAuthenticator() {
		if (!AuthProperties.getInstance().isHeaderConfigured()) {
			throw AuthExceptionType.AUTH_NOT_CONFIGURED.createException();
		}
	}
	
	public UserContext headerAuth(HttpServletRequest request) {
		String headerAttribute = AuthProperties.getInstance().getHeaderAttributeName();
		String username = request.getHeader(headerAttribute);
		try{
			if (username != null){
				return AuthenticationUtils.systemAuth(username);
			}
		} catch(Throwable e){
		}
		return null;
	}

	public UserContext jsonRpcAuth(String username, String unencryptedPassword) {
		return null;
	}

	public boolean wsAuth(WSPasswordCallback pwcb) {		
		return false;
	}

	public boolean canChangePassword() {
		return false;
	}

	public void changePassword(String username, String oldPassword, String newPassword) {
		throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
	}

}
