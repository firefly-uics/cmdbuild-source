package org.cmdbuild.dms.exception;

public class InvalidLoginException extends FtpException {

	private static final long serialVersionUID = 1L;

	public InvalidLoginException() {
		super();
	}

	public InvalidLoginException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public InvalidLoginException(final String message) {
		super(message);
	}

	public InvalidLoginException(final Throwable cause) {
		super(cause);
	}

	public static InvalidLoginException newInstance(final String username, final String password) {
		final String message = String.format("error logging in with '%s'/'%s'", username, password);
		return new InvalidLoginException(message);
	}

}
