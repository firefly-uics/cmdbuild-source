package org.cmdbuild.services.auth;

import java.util.Set;

import org.apache.commons.lang.Validate;

public class UserContextToUserInfo extends UserInfoBuilder {

	// private final UserContext userContext;

	public UserContextToUserInfo(/* final UserContext userContext */) {
		// FIXME
		// Validate.notNull(userContext, "null user context");
		// this.userContext = userContext;
	}

	@Override
	public UserInfo build() {
		setUsername(getUsername());
		setUserType(getUserType());
		setUserGroups(getUserGroups());
		return super.build();
	}

	private String getUsername() {
		return null; // FIXME was userContext.getRequestedUsername();
	}

	private UserType getUserType() {
		return null; // FIXME was userContext.getUserType();
	}

	private Set<UserGroup> getUserGroups() {
		final Set<UserGroup> userGroups = UserContextToUserInfo.aUserGroups();
		// FIXME
		// for (final Group group : userContext.getGroups()) {
		// userGroups.add(UserContextToUserInfo.aUserGroup(group));
		// }
		return userGroups;
	}

	// FIXME
	// public static UserContextToUserInfo newInstance(final UserContext
	// userContext) {
	// return null; // FIXME was new UserContextToUserInfo(userContext);
	// }

	// FIXME
	// public static UserGroup aUserGroup(final Group group) {
	// return aUserGroup(group.getName(), group.getDescription());
	// }

}
