package org.cmdbuild.portlet.exception;

public class EmailException extends CMDBuildPortletException{

    private EmailExceptionType type;

    public enum EmailExceptionType {
        ADDRESS_EXCEPTION,
        EMAIL_DESTINATION,
        MESSAGE_EXCEPTION,
        USER_EMAIL_NOT_FOUND;

        public EmailException createException(String... parameters) {
            return new EmailException(this, parameters);
        }
    }

    private EmailException(EmailExceptionType type, String... parameters) {
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
