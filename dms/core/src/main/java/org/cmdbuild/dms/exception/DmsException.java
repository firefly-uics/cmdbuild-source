package org.cmdbuild.dms.exception;

/**
 * Base exception for DMS service.
 */
public class DmsException extends Exception {

	private static final long serialVersionUID = 1L;

	protected DmsException() {
		super();
	}

	protected DmsException(final String message, final Throwable cause) {
		super(message, cause);
	}

	protected DmsException(final String message) {
		super(message);
	}

	protected DmsException(final Throwable cause) {
		super(cause);
	}

}
