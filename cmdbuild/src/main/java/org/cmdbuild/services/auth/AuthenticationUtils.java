package org.cmdbuild.services.auth;

import org.cmdbuild.config.AuthProperties;
import org.cmdbuild.elements.wrappers.UserCard;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.services.WorkflowService;
import org.cmdbuild.utils.SecurityEncrypter;

public class AuthenticationUtils {
	
	static UserContext systemAuth(String username) {
		checkIfNotServiceUser(username);
		User user = getUser(username);
		return new UserContext(user);
	}
	
	static void checkIfServiceUser(String usernameForAuthentication) {
		if (!isServiceUser(usernameForAuthentication))
			throw AuthExceptionType.AUTH_LOGIN_WRONG.createException();
	}

	static void checkIfNotServiceUser(String usernameForAuthentication) {
		if (isServiceUser(usernameForAuthentication))
			throw AuthExceptionType.AUTH_LOGIN_WRONG.createException();
	}
	
	static boolean isServiceUser(String usernameForAuthentication) {
		if (isSharkUser(usernameForAuthentication)) {
			return true;
		}
		String[] serviceUsers = AuthProperties.getInstance().getServiceUsers();
		for (int i=0, n=serviceUsers.length; i<n; ++i) {
			if (usernameForAuthentication.equals(serviceUsers[i]))
				return true;
		}
		return false;
	}
	
	static boolean isSharkUser(String usernameForAuthentication) {
		return WorkflowService.getInstance().getSharkWSUser().equals(usernameForAuthentication);
	}
	
	static User getUser(String username) {
		try {
			return UserCard.findByUserName(username);
		} catch (CMDBException e) {
			throw AuthExceptionType.AUTH_LOGIN_WRONG.createException();
		}
	}
	
	static void checkPassword(User user, String unencryptedPassword) {
		SecurityEncrypter se = new SecurityEncrypter();
		String encpass = se.encrypt(unencryptedPassword);
		if (!user.getEncryptedPassword().equals(encpass))
			throw AuthExceptionType.AUTH_WRONG_PASSWORD.createException();
	}
	
	static String getUnencryptedPassword(String username) {
		if (isSharkUser(username)) {
			return WorkflowService.getInstance().getSharkWSPassword();
		}
		User user = getUser(username);
		SecurityEncrypter enc = new SecurityEncrypter();
		String encryptedPassword = user.getEncryptedPassword();
		String unencryptedPassword = enc.decrypt(encryptedPassword);
		return unencryptedPassword;
	}
	
	static String getUsernameForAuthentication(String authData) {
		if (authData.contains("#")) {
			String usernameForAuthentication = authData.split("#")[0];
			checkIfServiceUser(usernameForAuthentication);
			return usernameForAuthentication;
		} else {
			return getUsernameNotService(authData);
		}
	}

	static String getUsernameNotService(String authData) {
		if (authData.contains("@")) {
			return authData.split("@")[0];
		} else {
			return authData;
		}
	}
	
	static String getUsername(String authData) {
		String notServicePart;
		if (authData.contains("#")) {
			notServicePart = authData.split("#")[1];
		} else {
			notServicePart = authData;
		}
		return getUsernameNotService(notServicePart);
	}
	
	static String getRole(String authData) {
		if (authData.contains("@")){
			return authData.split("@")[1];
		} else {
			return "";
		}
	}
	
	static void checkOrSetDefaultGroup(String authData, UserContext userCtx) {
		Group defaultGroup = null;
		try {
			defaultGroup = userCtx.getDefaultGroup();
		} catch (AuthException e) {
			String usergroup = getRole(authData);
			for (Group group : userCtx.getGroups()) {
				if (group.getName().equals(usergroup)) {
					defaultGroup = group;
					userCtx.setDefaultGroup(group.getId());
					break;
				}
			}
			if (defaultGroup == null) {
				throw e;
			}
		}
	}

}
