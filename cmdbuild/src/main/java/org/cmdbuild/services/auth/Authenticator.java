package org.cmdbuild.services.auth;

import javax.servlet.http.HttpServletRequest;

import org.apache.ws.security.WSPasswordCallback;
import org.cmdbuild.exception.RedirectException;


public interface Authenticator {
	UserContext jsonRpcAuth(String username, String unencryptedPassword);
	boolean wsAuth(WSPasswordCallback pwcb);
	UserContext headerAuth(HttpServletRequest request) throws RedirectException;
	void changePassword(String username, String oldPassword, String newPassword);
	boolean canChangePassword();
	boolean allowsPasswordLogin();
}
