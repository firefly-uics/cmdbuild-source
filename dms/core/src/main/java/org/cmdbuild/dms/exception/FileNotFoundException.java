package org.cmdbuild.dms.exception;

public class FileNotFoundException extends DmsException {

	private static final long serialVersionUID = 1L;

	public FileNotFoundException() {
		super();
	}

	public FileNotFoundException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public FileNotFoundException(final String message) {
		super(message);
	}

	public FileNotFoundException(final Throwable cause) {
		super(cause);
	}

	public static FileNotFoundException newInstance(final String fileName, final String className, final int cardId) {
		final String message = String.format("file '%s' not found for class '%s' with id '%d'", fileName, className,
				cardId);
		return new FileNotFoundException(message);
	}

}
