package org.cmdbuild.dms.exception;

public class MissingPropertiesException extends DmsRuntimeException {

	private static final long serialVersionUID = 1L;

	public MissingPropertiesException() {
		super();
	}

	public MissingPropertiesException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public MissingPropertiesException(final String message) {
		super(message);
	}

	public MissingPropertiesException(final Throwable cause) {
		super(cause);
	}

}
