package org.cmdbuild.connector.ws;

import org.cmdbuild.connector.ConnectorException;

public class SyncException extends ConnectorException {

	private static final long serialVersionUID = 1L;

	public SyncException() {
		super();
	}

	public SyncException(final String message) {
		super(message);
	}

	public SyncException(final Throwable cause) {
		super(cause);
	}

	public SyncException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
