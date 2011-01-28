package org.cmdbuild.portlet.exception;

public class WebserviceException extends CMDBuildPortletException {

    private WebserviceExceptionType type;

    public enum WebserviceExceptionType {
        WEBSERVICE_CONFIGURATION,
        WEBSERVICE_RESPONSE;

        public WebserviceException createException(String... parameters) {
            return new WebserviceException(this, parameters);
        }
    }

    private WebserviceException(WebserviceExceptionType type, String... parameters) {
        this.type = type;
        this.parameters = parameters;
    }

    public WebserviceExceptionType getExceptionType() {
        return this.type;
    }

    @Override
    public String getExceptionTypeText() {
        return this.type.toString();
    }
}
