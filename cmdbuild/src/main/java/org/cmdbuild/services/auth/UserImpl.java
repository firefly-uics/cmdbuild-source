package org.cmdbuild.services.auth;

public class UserImpl implements User {

	int id;
	String username;
	String description;
	String encPassword;

	public static final String SYSTEM_USERNAME = "system";
	static private final User systemUser = new UserImpl(0, SYSTEM_USERNAME, "System User", "");

	public UserImpl(int id, String username, String description, String encPassword) {
		this.id = id;
		this.username = username;
		this.description = description;
		this.encPassword = encPassword;
	}

	public static User getSystemUser() {
		return systemUser;
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
