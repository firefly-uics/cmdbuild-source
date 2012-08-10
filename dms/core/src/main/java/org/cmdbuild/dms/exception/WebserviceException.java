package org.cmdbuild.dms.exception;

public class WebserviceException extends DmsException {

	private static final long serialVersionUID = 1L;

	public WebserviceException() {
		super();
	}

	public WebserviceException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public WebserviceException(final String message) {
		super(message);
	}

	public WebserviceException(final Throwable cause) {
		super(cause);
	}

}
