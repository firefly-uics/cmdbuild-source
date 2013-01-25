package org.cmdbuild.model.widget.service.soap.exception;

public class ConnectionException extends RuntimeException {

	public ConnectionException() {
		super();
	}

	public ConnectionException(final String message) {
		super(message);
	}

	public ConnectionException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
