package org.cmdbuild.exception;


public class WidgetException extends CMDBException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private WidgetExceptionType type;

	public enum WidgetExceptionType {
		WIDGET_SERVICE_MALFORMED_REQUEST,
		WIDGET_SERVICE_CONNECTION_ERROR;

		public WidgetException createException(String ... parameters) {
			return new WidgetException(this, parameters);
		}
	}

	private WidgetException(WidgetExceptionType type, String ... parameters) {
		this.type = type;
		this.parameters = parameters;
	}

	public WidgetExceptionType getExceptionType() {
		return this.type;
	}
	
	public String getExceptionTypeText() {
		return this.type.toString();
	}

}
