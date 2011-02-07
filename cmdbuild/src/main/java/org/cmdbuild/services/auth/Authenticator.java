package org.cmdbuild.services.auth;

import javax.servlet.http.HttpServletRequest;

import org.apache.ws.security.WSPasswordCallback;


public interface Authenticator {
	UserContext jsonRpcAuth(String username, String unencryptedPassword);
	boolean wsAuth(WSPasswordCallback pwcb);
	UserContext headerAuth(HttpServletRequest request);
	void changePassword(String username, String oldPassword, String newPassword);
	boolean canChangePassword();
}
