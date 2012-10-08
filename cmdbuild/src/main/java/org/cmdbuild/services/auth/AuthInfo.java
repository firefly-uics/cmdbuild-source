package org.cmdbuild.services.auth;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.cmdbuild.config.AuthProperties;
import org.cmdbuild.elements.wrappers.UserCard;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.logger.Log;

public class AuthInfo {

	private static final Logger logger = Log.AUTH;

	private static final String PATTERN = "([^@#]+(@[^\\.]+\\.[^@#]+)?)(#([^@]+(@[^\\.]+\\.[^@]+)?))?(@([^@\\.]+))?";

	private final String user;
	private final String userNotService;
	private final String role;

	private final String usernameForAuthentication;

	public AuthInfo(final String authData) {
		Validate.notEmpty(authData);

		final Pattern pattern = Pattern.compile(PATTERN);
		final Matcher matcher = pattern.matcher(authData);
		if (!matcher.find()) {
			throw AuthExceptionType.AUTH_LOGIN_WRONG.createException();
		}
		user = matcher.group(1);
		userNotService = matcher.group(4);
		role = matcher.group(7);

		usernameForAuthentication = hasServiceUser() ? user : getUsername();

		logger.debug(this);
	}

	public boolean hasServiceUser() {
		return StringUtils.isNotEmpty(userNotService);
	}

	public String getUsernameForAuthentication() {
		if (hasServiceUser() && !isValidServiceUser()) {
			throw AuthExceptionType.AUTH_LOGIN_WRONG.createException();
		}
		return usernameForAuthentication;
	}

	public String getUsername() {
		return StringUtils.defaultIfBlank(userNotService, user);
	}

	public String getRole() {
		return StringUtils.defaultIfBlank(role, StringUtils.EMPTY);
	}

	private boolean isValidServiceUser() {
		return getServiceUsers().contains(usernameForAuthentication);
	}

	/*
	 * Overridden for testing purposes
	 */
	protected List<String> getServiceUsers() {
		return AuthProperties.getInstance().getServiceUsers();
	}

	public boolean isPrivilegedServiceUser() {
		return getPrivilegedServiceUsers().contains(usernameForAuthentication);
	}

	/*
	 * Overridden for testing purposes
	 */
	protected List<String> getPrivilegedServiceUsers() {
		return AuthProperties.getInstance().getPrivilegedServiceUsers();
	}

	public void checkOrSetDefaultGroup(final UserContext userCtx) {
		final String usergroup = getRole();
		for (final Group group : userCtx.getGroups()) {
			if (group.getName().equals(usergroup)) {
				logger.debug(String.format("selected group '%s'", group.getName()));
				userCtx.setDefaultGroup(group.getId());
				break;
			}
		}
	}

	public UserContext systemAuth() {
		if (isValidServiceUser()) {
			throw AuthExceptionType.AUTH_LOGIN_WRONG.createException();
		}
		final User user = UserCard.getUser(getUsername());
		return new UserContext(user);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("user", user).append("userNotService", userNotService).append("role",
				role).toString();
	}

}
