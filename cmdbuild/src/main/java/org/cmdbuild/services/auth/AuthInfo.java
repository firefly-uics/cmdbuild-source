package org.cmdbuild.services.auth;

import java.util.Set;
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
import org.cmdbuild.services.WorkflowService;

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
		if (isSharkUser()) {
			return true;
		}
		for (final String serviceUser : getServiceUsers()) {
			if (usernameForAuthentication.equals(serviceUser)) {
				return true;
			}
		}
		return false;
	}

	protected Set<String> getServiceUsers() {
		return AuthProperties.getInstance().getServiceUsers();
	}

	public boolean isSharkUser() {
		return getSharkWSUser().equals(usernameForAuthentication);
	}

	protected String getSharkWSUser() {
		return WorkflowService.getInstance().getSharkWSUser();
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
