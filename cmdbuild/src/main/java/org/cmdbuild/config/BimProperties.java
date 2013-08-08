package org.cmdbuild.config;

import static org.apache.commons.lang.StringUtils.EMPTY;

import org.cmdbuild.services.Settings;
import org.cmdbuild.bim.service.bimserver.BimserverService;

public class BimProperties extends DefaultProperties implements BimserverService.Configuration {

	private static final long serialVersionUID = 1L;

	private static final String	MODULE_NAME = "bim",
								ENABLED = "enabled",
								URL = "url",
								USERNAME = "username",
								PASSWORD = "password";

	public BimProperties() {
		super();
		setProperty(ENABLED, "false");
		setProperty(URL, EMPTY);
		setProperty(USERNAME, EMPTY);
		setProperty(PASSWORD, EMPTY);
	}

	public static BimProperties getInstance() {
		return (BimProperties) Settings.getInstance().getModule(MODULE_NAME);
	}

	public boolean isEnabled() {
		return Boolean.parseBoolean(getProperty(ENABLED));
	}

	@Override
	public String getUsername() {
		return getProperty(USERNAME);
	}

	@Override
	public String getPassword() {
		return getProperty(PASSWORD);
	}

	@Override
	public String getUrl() {
		return getProperty(URL);
	}

}
