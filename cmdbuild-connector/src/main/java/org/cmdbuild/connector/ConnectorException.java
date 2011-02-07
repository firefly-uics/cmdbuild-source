package org.cmdbuild.connector;

public class ConnectorException extends Exception {

	private static final long serialVersionUID = 1L;

	public ConnectorException() {
		super();
	}

	public ConnectorException(final String message) {
		super(message);
	}

	public ConnectorException(final Throwable cause) {
		super(cause);
	}

	public ConnectorException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
