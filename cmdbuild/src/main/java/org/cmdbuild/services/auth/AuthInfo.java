package org.cmdbuild.services.auth;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.cmdbuild.config.AuthProperties;
import org.cmdbuild.elements.wrappers.UserCard;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.services.WorkflowService;

public class AuthInfo {

	public static final String USERS_SEPARATOR = "#";
	public static final String GROUP_SEPARATOR = "@";

	private final String authData;
	private String usernameForAuthentication;

	public AuthInfo(final String authData) {
		Validate.notEmpty(authData);
		this.authData = authData;
		initUsernameForAuthentication();
	}

	private void initUsernameForAuthentication() {
		if (authData.contains(USERS_SEPARATOR)) {
			usernameForAuthentication = authData.split(USERS_SEPARATOR)[0];
		} else {
			usernameForAuthentication = getUsernameNotService(authData);
		}
	}

	public String getUsernameForAuthentication() {
		if (authData.contains(USERS_SEPARATOR)) {
			checkIfServiceUser();
			return usernameForAuthentication;
		} else {
			return usernameForAuthentication;
		}
	}

	public String getUsername() {
		String notServicePart;
		if (authData.contains(USERS_SEPARATOR)) {
			notServicePart = authData.split(USERS_SEPARATOR)[1];
		} else {
			notServicePart = authData;
		}
		return getUsernameNotService(notServicePart);
	}

	public String getRole() {
		if (authData.contains(GROUP_SEPARATOR)) {
			return authData.split(GROUP_SEPARATOR)[1];
		} else {
			return StringUtils.EMPTY;
		}
	}

	private String getUsernameNotService(final String notServicePart) {
		if (notServicePart.contains(GROUP_SEPARATOR)) {
			return notServicePart.split(GROUP_SEPARATOR)[0];
		} else {
			return notServicePart;
		}
	}

	private void checkIfServiceUser() {
		if (!isServiceUser())
			throw AuthExceptionType.AUTH_LOGIN_WRONG.createException();
	}

	private void checkIfNotServiceUser() {
		if (isServiceUser())
			throw AuthExceptionType.AUTH_LOGIN_WRONG.createException();
	}

	private boolean isServiceUser() {
		if (isSharkUser()) {
			return true;
		}
		final String[] serviceUsers = getServiceUsers();
		for (int i = 0, n = serviceUsers.length; i < n; ++i) {
			if (usernameForAuthentication.equals(serviceUsers[i]))
				return true;
		}
		return false;
	}

	protected String[] getServiceUsers() {
		return AuthProperties.getInstance().getServiceUsers();
	}

	public boolean isSharkUser() {
		return getSharkWSUser().equals(usernameForAuthentication);
	}

	protected String getSharkWSUser() {
		return WorkflowService.getInstance().getSharkWSUser();
	}

	public void checkOrSetDefaultGroup(final UserContext userCtx) {
		Group defaultGroup = null;
		try {
			defaultGroup = userCtx.getDefaultGroup();
		} catch (final AuthException e) {
			final String usergroup = getRole();
			for (final Group group : userCtx.getGroups()) {
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

	public UserContext systemAuth() {
		checkIfNotServiceUser();
		final User user = UserCard.findByUserName(getUsername());
		return new UserContext(user);
	}

}
