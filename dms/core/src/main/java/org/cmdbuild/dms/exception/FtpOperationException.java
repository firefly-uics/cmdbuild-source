package org.cmdbuild.dms.exception;

public class FtpOperationException extends FtpException {

	private static final long serialVersionUID = 1L;

	public FtpOperationException() {
		super();
	}

	public FtpOperationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public FtpOperationException(final String message) {
		super(message);
	}

	public FtpOperationException(final Throwable cause) {
		super(cause);
	}

}
