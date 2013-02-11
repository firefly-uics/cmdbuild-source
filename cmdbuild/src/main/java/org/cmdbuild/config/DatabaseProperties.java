package org.cmdbuild.config;

import org.cmdbuild.services.Settings;

public class DatabaseProperties extends DefaultProperties {

	private static final long serialVersionUID = 1L;

	private static final String MODULE_NAME = "database";

	private static final String URL = "db.url";
	private static final String USERNAME = "db.username";
	private static final String PASSWORD = "db.password";
	private static final String BACKEND_CLASS = "db.backend";

	public DatabaseProperties() {
		super();
		clearConfiguration();
	}

	public static DatabaseProperties getInstance() {
		return (DatabaseProperties) Settings.getInstance().getModule(MODULE_NAME);
	}

	public void clearConfiguration() {
		setProperty(URL, "");
		setProperty(USERNAME, "");
		setProperty(PASSWORD, "");
	}

	public boolean isConfigured() {
		return !("".equals(getDatabaseUrl()) || "".equals(getDatabasePassword()) || "".equals(getDatabaseUser()));
	}

	public String getDatabaseUrl() {
		return getProperty(URL);
	}

	public void setDatabaseUrl(String databaseUrl) {
		setProperty(URL, databaseUrl);
	}

	public String getDatabaseUser() {
		return getProperty(USERNAME);
	}

	public void setDatabaseUser(String databaseUser) {
		setProperty(USERNAME, databaseUser);
	}

	public String getDatabasePassword() {
		return getProperty(PASSWORD);
	}

	public void setDatabasePassword(String databasePassword) {
		setProperty(PASSWORD, databasePassword);
	}

	public String getDatabaseBackendClass() {
		return getProperty(BACKEND_CLASS, "org.cmdbuild.dao.backend.postgresql.PGCMBackend");
	}
}
