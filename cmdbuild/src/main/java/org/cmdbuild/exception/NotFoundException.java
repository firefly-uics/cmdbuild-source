package org.cmdbuild.exception;

public class NotFoundException extends CMDBException {

	private static final long serialVersionUID = 1L;
	private NotFoundExceptionType type;
	
	public enum NotFoundExceptionType {
		NOTFOUND,
		CLASS_NOTFOUND,           // class id or desc
		DOMAIN_NOTFOUND,          // domain id or desc
		LOOKUP_TYPE_NOTFOUND,     // lookup type name
		ATTRIBUTE_NOTFOUND,       // attribute name
		CARD_NOTFOUND,            // class desc
		LOOKUP_NOTFOUND,          // lookup name
		MENU_NOTFOUND,
		ATTACHMENT_NOTFOUND,      // class name, card id, attachment name
		PRIVILEGE_NOTFOUND,       // group id, class id
		PRIVILEGE_GROUP_NOTFOUND, // group id
		PARAMETER_CLASS_UNAVAILABLE,
		SERVICE_UNAVAILABLE;

		public NotFoundException createException(String ... parameters){
			return new NotFoundException(this, parameters);
		}
	}

	private NotFoundException(NotFoundExceptionType type, String ... parameters) {
		this.type = type;
		this.parameters = parameters;
	}

	public NotFoundExceptionType getExceptionType() {
		return this.type;
	}
	
	public String getExceptionTypeText() {
		return this.type.toString();
	}
}
