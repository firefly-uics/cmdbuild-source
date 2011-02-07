package org.cmdbuild.connector.parser;

import org.cmdbuild.connector.ConnectorException;

public class ParserException extends ConnectorException {

	private static final long serialVersionUID = 1L;

	public ParserException() {
		super();
	}

	public ParserException(final String message) {
		super(message);
	}

	public ParserException(final Throwable cause) {
		super(cause);
	}

	public ParserException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
