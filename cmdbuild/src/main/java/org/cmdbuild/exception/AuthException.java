package org.cmdbuild.exception;

public class AuthException extends CMDBException {

	private static final long serialVersionUID = 1L;
	
	private AuthExceptionType type;
	
	public enum AuthExceptionType {
		AUTH_LOGIN_WRONG,
		AUTH_WRONG_PASSWORD,
		AUTH_NO_GROUPS,
		AUTH_MULTIPLE_GROUPS,
		AUTH_UNKNOWN_GROUP,
		AUTH_NOT_AUTHORIZED,
		AUTH_DEMO_MODE,
		AUTH_NOT_CONFIGURED,
		AUTH_NOT_LOGGED_IN,
		AUTH_CLASS_NOT_AUTHORIZED,  // class name
		AUTH_DOMAIN_NOT_AUTHORIZED; // domain name

		public AuthException createException(String ... parameters){
			return new AuthException(this, parameters);
		}
	}

	private AuthException(AuthExceptionType type, String ... parameters) {
		this.type = type;
		this.parameters = parameters;
	}

	public AuthExceptionType getExceptionType() {
		return this.type;
	}
	
	public String getExceptionTypeText() {
		return this.type.toString();
	}
}
