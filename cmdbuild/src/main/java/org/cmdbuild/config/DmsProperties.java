package org.cmdbuild.config;

import org.cmdbuild.services.Settings;

public class DmsProperties extends DefaultProperties implements org.cmdbuild.dms.properties.DmsProperties {

	private static final long serialVersionUID = 1L;

	private static final String MODULE_NAME = "dms";

	private static final String ENABLED = "enabled";
	private static final String SERVER_URL = "server.url";
	private static final String FILE_SERVER_PORT = "fileserver.port";
	private static final String FILE_SERVER_TYPE = "fileserver.type";
	private static final String FILE_SERVER_URL = "fileserver.url";
	/*
	 * wspath is the path for the base space, fspath is the same thing, in terms
	 * of directories
	 */
	private static final String REPOSITORY_FS_PATH = "repository.fspath";
	private static final String REPOSITORY_WS_PATH = "repository.wspath";
	private static final String REPOSITORY_APP = "repository.app";
	private static final String PASSWORD = "credential.password";
	private static final String USER = "credential.user";
	private static final String CATEGORY_LOOKUP = "category.lookup";
	private static final String DELAY = "delay";

	public DmsProperties() {
		super();
		setProperty(ENABLED, String.valueOf(false));
		setProperty(SERVER_URL, "http://localhost:8181/alfresco/api");
		setProperty(FILE_SERVER_PORT, "1121");
		setProperty(FILE_SERVER_URL, "localhost");
		setProperty(REPOSITORY_FS_PATH, "/Alfresco/User Homes/cmdbuild");
		setProperty(REPOSITORY_WS_PATH, "/app:company_home/app:user_homes/");
		setProperty(REPOSITORY_APP, "cm:cmdbuild");
		setProperty(PASSWORD, "admin");
		setProperty(USER, "admin");
		setProperty(CATEGORY_LOOKUP, "AlfrescoCategory");
		setProperty(DELAY, "1000");
	}

	public static DmsProperties getInstance() {
		return (DmsProperties) Settings.getInstance().getModule(MODULE_NAME);
	}

	public boolean isEnabled() {
		String enabled = getProperty(ENABLED, "false");
		return enabled.equals("true");
	}

	public String getServerURL() {
		return getProperty(SERVER_URL);
	}

	public void setServerURL(String url) {
		setProperty(SERVER_URL, url);
	}

	public String getFtpPort() {
		return getProperty(FILE_SERVER_PORT);
	}

	public void setFtpPort(String port) {
		setProperty(FILE_SERVER_PORT, port);
	}

	public String getFtpHost() {
		return getProperty(FILE_SERVER_URL);
	}

	public void setFtpHost(String hostname) {
		setProperty(FILE_SERVER_URL, hostname);
	}

	public void setFtpType(String type) {
		setProperty(FILE_SERVER_TYPE, type);
	}

	public String getAlfrescoUser() {
		return getProperty(USER);
	}

	public void setAlfrescoUser(String username) {
		setProperty(USER, username);
	}

	public String getAlfrescoPassword() {
		return getProperty(PASSWORD);
	}

	public void setAlfrescoPassword(String password) {
		setProperty(PASSWORD, password);
	}

	public String getCmdbuildCategory() {
		return getProperty(CATEGORY_LOOKUP);
	}

	public void setCmdbuildCategory(String category) {
		setProperty(CATEGORY_LOOKUP, category);
	}

	public String getRepositoryFSPath() {
		return getProperty(REPOSITORY_FS_PATH);
	}

	public void setRepositoryFSPath(String repository) {
		setProperty(REPOSITORY_FS_PATH, repository);
	}

	public String getRepositoryWSPath() {
		return getProperty(REPOSITORY_WS_PATH);
	}

	public void setRepositoryWSPath(String repository) {
		setProperty(REPOSITORY_WS_PATH, repository);
	}

	public String getRepositoryApp() {
		return getProperty(REPOSITORY_APP);
	}

	public void setRepositoryApp(String repository) {
		setProperty(REPOSITORY_APP, repository);
	}
}
