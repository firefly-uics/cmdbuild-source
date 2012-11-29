package org.cmdbuild.services.auth;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;

public class UserInfoBuilder {

	private String username;
	private UserType userType;
	private Set<UserGroup> userGroups;

	public UserInfoBuilder setUsername(final String username) {
		this.username = username;
		return this;
	}

	public UserInfoBuilder setUserType(final UserType userType) {
		this.userType = userType;
		return this;
	}

	public UserInfoBuilder setUserGroups(final Set<UserGroup> userGroups) {
		if (userGroups == null) {
			this.userGroups = null;
		} else {
			this.userGroups = createUserGroups();
			this.userGroups.addAll(userGroups);
		}
		return this;
	}

	public UserInfoBuilder addUserGroup(final UserGroup userGroup) {
		if (userGroups == null) {
			userGroups = createUserGroups();
		}
		userGroups.add(userGroup);
		return this;
	}

	private static Set<UserGroup> createUserGroups() {
		return new HashSet<UserGroup>();
	}

	public UserInfo build() {
		validateUsername();
		validateUserType();
		validateUserGroups();
		return create();
	}

	private void validateUsername() {
		Validate.isTrue(isNotBlank(username), format("invalid username '%s'", username));
	}

	private void validateUserType() {
		Validate.notNull(userType, "null user type");
	}

	private void validateUserGroups() {
		Validate.notNull(userGroups, "null user groups");
	}

	public final UserInfo create() {
		final UserInfo userInfo = new UserInfo();
		userInfo.setUsername(username);
		userInfo.setUserType(userType);
		userInfo.setGroups(userGroups);
		return userInfo;
	}

	public static UserGroup aUserGroup(final String name, final String description) {
		final UserGroup userGroup = new UserGroup();
		userGroup.setName(name);
		userGroup.setDescription(description);
		return userGroup;
	}

	public static Set<UserGroup> aUserGroups(final UserGroup... userGroups) {
		final Set<UserGroup> set = createUserGroups();
		set.addAll(Arrays.asList(userGroups));
		return set;
	}

}
