package org.cmdbuild.exception;

public class ConsistencyException extends CMDBException {

	private static final long serialVersionUID = 1L;

	private final ConsistencyExceptionType type;

	public enum ConsistencyExceptionType {
		/**
		 * Parameters must be defined in the following order: lockerUsername,
		 * timeSinceLock
		 */
		LOCKED_CARD, //
		CARD_NO_MORE_LOCKED;

		public ConsistencyException createException(final String... parameters) {
			return new ConsistencyException(this, parameters);
		}
	}

	private ConsistencyException(final ConsistencyExceptionType type, final String... parameters) {
		this.type = type;
		this.parameters = parameters;
	}

	public ConsistencyExceptionType getExceptionType() {
		return this.type;
	}

	@Override
	public String getExceptionTypeText() {
		return this.type.toString();
	}
}
