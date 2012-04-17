package org.cmdbuild.dms.exception;

public class ConnectionException extends FtpException {

	private static final long serialVersionUID = 1L;

	public ConnectionException() {
		super();
	}

	public ConnectionException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ConnectionException(final String message) {
		super(message);
	}

	public ConnectionException(final Throwable cause) {
		super(cause);
	}

	public static ConnectionException newInstance(final String host, final String port) {
		final String message = String.format("error connecting to '%s:%s'", host, port);
		return new ConnectionException(message);
	}

}
