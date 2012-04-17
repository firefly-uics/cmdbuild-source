package org.cmdbuild.dms.exception;

/**
 * Base exception for FTP operations.
 */
public class FtpException extends DmsException {

	private static final long serialVersionUID = 1L;

	protected FtpException() {
		super();
	}

	protected FtpException(final String message, final Throwable cause) {
		super(message, cause);
	}

	protected FtpException(final String message) {
		super(message);
	}

	protected FtpException(final Throwable cause) {
		super(cause);
	}

}
