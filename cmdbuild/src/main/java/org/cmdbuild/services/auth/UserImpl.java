package org.cmdbuild.services.auth;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import org.apache.commons.lang.Validate;

public class UserImpl implements User {

	private final int id;
	private final String username;
	private final String description;
	private final String encPassword;

	public static final int SYSTEM_USER_ID = 0;
	public static final String SYSTEM_USER_USERNAME = "system";
	public static final String SYSTEM_USER_DESCRIPTION = "System User";
	public static final User SYSTEM_USER = createSystemUser(SYSTEM_USER_USERNAME);

	public UserImpl(final int id, final String username, final String description, final String encPassword) {
		Validate.isTrue(id >= 0, "invalid id");
		Validate.isTrue(isNotBlank(username), String.format("invalid username '%s'", username));
		this.id = id;
		this.username = username;
		this.description = description;
		this.encPassword = encPassword;
	}

	public static User getSystemUser() {
		return SYSTEM_USER;
	}

	public static User getElevatedPrivilegesUser(final String login) {
		final String elevatedPrivilegeLogin = String.format("%s / %s", SYSTEM_USER_USERNAME, login);
		return createSystemUser(elevatedPrivilegeLogin);
	}

	private static User createSystemUser(final String username) {
		return new UserImpl(SYSTEM_USER_ID, username, SYSTEM_USER_DESCRIPTION, EMPTY);
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return username;
	}

	public String getDescription() {
		return description;
	}

	public String getEncryptedPassword() {
		return encPassword;
	}

	public String toString() {
		return getDescription();
	}

}
