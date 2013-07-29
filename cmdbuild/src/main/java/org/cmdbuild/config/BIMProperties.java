package org.cmdbuild.config;

import static org.apache.commons.lang.StringUtils.EMPTY;

import org.cmdbuild.services.Settings;

@SuppressWarnings("serial")
public class BIMProperties extends DefaultProperties {

	private static final String	MODULE_NAME = "bim",
								ENABLED = "enabled",
								URL = "url",
								USERNAME = "username",
								PASSWORD = "password";

	public BIMProperties() {
		super();
		setProperty(ENABLED, "false");
		setProperty(URL, EMPTY);
		setProperty(USERNAME, EMPTY);
		setProperty(PASSWORD, EMPTY);
	}

	public static BIMProperties getInstance() {
		return (BIMProperties) Settings.getInstance().getModule(MODULE_NAME);
	}

	public boolean isEnabled() {
		return Boolean.parseBoolean(getProperty(ENABLED));
	}

	public String getUsername() {
		return getProperty(USERNAME);
	}

	public String getPassword() {
		return getProperty(PASSWORD);
	}

	public String getURL() {
		return getProperty(URL);
	}
}
