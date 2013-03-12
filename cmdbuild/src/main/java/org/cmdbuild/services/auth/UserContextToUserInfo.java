package org.cmdbuild.services.auth;

import java.util.Set;

import org.apache.commons.lang.Validate;

public class UserContextToUserInfo extends UserInfoBuilder {

	private final UserContext userContext;

	public UserContextToUserInfo(final UserContext userContext) {
		Validate.notNull(userContext, "null user context");
		this.userContext = userContext;
	}

	@Override
	public UserInfo build() {
		setUsername(getUsername());
		setUserType(getUserType());
		setUserGroups(getUserGroups());
		return super.build();
	}

	private String getUsername() {
		return userContext.getRequestedUsername();
	}

	private UserType getUserType() {
		return userContext.getUserType();
	}

	private Set<UserGroup> getUserGroups() {
		final Set<UserGroup> userGroups = UserContextToUserInfo.aUserGroups();
		for (final Group group : userContext.getGroups()) {
			userGroups.add(UserContextToUserInfo.aUserGroup(group));
		}
		return userGroups;
	}

	public static UserContextToUserInfo newInstance(final UserContext userContext) {
		return new UserContextToUserInfo(userContext);
	}

	public static UserGroup aUserGroup(final Group group) {
		return aUserGroup(group.getName(), group.getDescription());
	}
}
