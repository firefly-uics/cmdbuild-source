package org.cmdbuild.portlet.exception;

public class WebserviceException extends CMDBuildPortletException {

	private static final long serialVersionUID = 1L;

	private final WebserviceExceptionType type;

	public enum WebserviceExceptionType {
		WEBSERVICE_CONFIGURATION, WEBSERVICE_RESPONSE;

		public WebserviceException createException(final String... parameters) {
			return new WebserviceException(this, parameters);
		}
	}

	private WebserviceException(final WebserviceExceptionType type, final String... parameters) {
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
