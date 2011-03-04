package org.cmdbuild.connector.differ;

import org.cmdbuild.connector.ConnectorException;

public class DifferException extends ConnectorException {

	private static final long serialVersionUID = 1L;

	public DifferException() {
		super();
	}

	public DifferException(final String message) {
		super(message);
	}

	public DifferException(final Throwable cause) {
		super(cause);
	}

	public DifferException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
