package org.cmdbuild.portlet.exception;

public class EmailException extends CMDBuildPortletException {

	private static final long serialVersionUID = 1L;

	private final EmailExceptionType type;

	public enum EmailExceptionType {
		ADDRESS_EXCEPTION, EMAIL_DESTINATION, MESSAGE_EXCEPTION, USER_EMAIL_NOT_FOUND;

		public EmailException createException(final String... parameters) {
			return new EmailException(this, parameters);
		}
	}

	private EmailException(final EmailExceptionType type, final String... parameters) {
		this.type = type;
		this.parameters = parameters;
	}

	public EmailExceptionType getExceptionType() {
		return this.type;
	}

	@Override
	public String getExceptionTypeText() {
		return this.type.toString();
	}

}
