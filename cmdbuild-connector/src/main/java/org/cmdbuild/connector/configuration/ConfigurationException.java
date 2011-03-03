package org.cmdbuild.connector.configuration;

import org.cmdbuild.connector.ConnectorException;

public class ConfigurationException extends ConnectorException {

	private static final long serialVersionUID = 1L;

	public ConfigurationException() {
		super();
	}

	public ConfigurationException(final String message) {
		super(message);
	}

	public ConfigurationException(final Throwable cause) {
		super(cause);
	}

	public ConfigurationException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
