package org.cmdbuild.connector.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.PropertyConfigurator;

public class ConnectorConfiguration {

	public static final String CONFIGURATION_PATH_PROPERTY = "org.cmdbuild.connector.conf.path";

	private static ConnectorConfiguration instance;

	private static final String CONNECTOR_FILENAME = "connector.conf";
	private static final String LOG4J_FILENAME = "log4j.conf";

	private static final String CMDBUILD_URL = "cmdbuild.url";
	private static final String CMDBUILD_USERNAME = "cmdbuild.username";
	private static final String CMDBUILD_PASSWORD = "cmdbuild.password";
	private static final String EXTERNAL_MODULE = "connector.external_module";

	private static final String START_MESSAGE = "connector.message.start";
	private static final String END_MESSAGE = "connector.message.end";

	private final Properties properties;

	private ConnectorConfiguration() {
		properties = new Properties();
	}

	public static ConnectorConfiguration getInstance() {
		if (instance == null) {
			instance = new ConnectorConfiguration();
		}
		return instance;
	}

	public void load(final String path) throws ConfigurationException {
		final String configurationPath = path + (path.endsWith(File.separator) ? StringUtils.EMPTY : File.separator);

		PropertyConfigurator.configure(configurationPath + LOG4J_FILENAME);

		try {
			properties.load(new FileInputStream(configurationPath + CONNECTOR_FILENAME));
		} catch (final Exception e) {
			throw new ConfigurationException(e);
		}
	}

	private String getProperty(final String key) {
		Validate.notNull(key, "null key");
		final String value = properties.getProperty(key);
		return StringUtils.defaultString(value, StringUtils.EMPTY);
	}

	private void setProperty(final String key, final String value) {
		Validate.notNull(key, "null key");
		properties.setProperty(key, value);
	}

	public String getCMDBuildUsername() {
		return getProperty(CMDBUILD_USERNAME);
	}

	public void setCMDBuildUsername(final String value) {
		setProperty(CMDBUILD_USERNAME, value);
	}

	public String getCMDBuildPassword() {
		return getProperty(CMDBUILD_PASSWORD);
	}

	public void setCMDBuildPassword(final String value) {
		setProperty(CMDBUILD_PASSWORD, value);
	}

	public String getCMDBuildURL() {
		return getProperty(CMDBUILD_URL);
	}

	public void setCMDBuildURL(final String value) {
		setProperty(CMDBUILD_URL, value);
	}

	public String getExternalModule() {
		return getProperty(EXTERNAL_MODULE);
	}

	public void setExternalModule(final String value) {
		setProperty(EXTERNAL_MODULE, value);
	}

	public String getStartMessage() {
		return getProperty(START_MESSAGE);
	}

	public void setStartMessage(final String value) {
		setProperty(START_MESSAGE, value);
	}

	public String getEndMessage() {
		return getProperty(END_MESSAGE);
	}

	public void setEndMessage(final String value) {
		setProperty(END_MESSAGE, value);
	}

}
