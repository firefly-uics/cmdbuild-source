package org.cmdbuild.services.auth;

import javax.servlet.http.HttpServletRequest;

import org.apache.ws.security.WSPasswordCallback;
import org.cmdbuild.elements.wrappers.UserCard;
import org.cmdbuild.exception.AuthException.AuthExceptionType;

public class DBAuthenticator implements Authenticator{

	public UserContext headerAuth(HttpServletRequest request) {
		return null;
	}

	public UserContext jsonRpcAuth(String username, String unencryptedPassword) {
		UserContext userCtx = AuthenticationUtils.systemAuth(username);
		AuthenticationUtils.checkPassword(userCtx.getUser(), unencryptedPassword);
		return userCtx;
	}

	public boolean wsAuth(WSPasswordCallback pwcb) {
		String username = AuthenticationUtils.getUsernameForAuthentication(pwcb.getIdentifer());
		String unencryptedPassword = AuthenticationUtils.getUnencryptedPassword(username);
		if (isDigested(pwcb)){
			pwcb.setPassword(unencryptedPassword);
			// it should stop the authentication chain because it does not make sense
			// to have the password set multiple times (it is done here only, indeed)
			return true;
		} else {
			return checkPassword(pwcb, unencryptedPassword);
		}
	}

	private boolean checkPassword(WSPasswordCallback pwcb, String unencryptedPassword) {
		String messagePassword =  pwcb.getPassword();
		return (messagePassword.equals(unencryptedPassword)); 				
	}

	private boolean isDigested(WSPasswordCallback pwcb) {
		return (pwcb.getPassword() == null);
	}

	public void changePassword(String username, String oldPassword,
			String newPassword) {
		if (jsonRpcAuth(username, oldPassword) != null){
			UserCard user = UserCard.getUserCardByName(username);
		    user.setUnencryptedPassword(newPassword);
		    user.save();
		} else {
			throw AuthExceptionType.AUTH_WRONG_PASSWORD.createException();
		}
	}

	public boolean canChangePassword() {
		return true;
	}

}
