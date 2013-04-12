package org.cmdbuild.exception;


public class StoredProcedureException extends CMDBException {

	private static final long serialVersionUID = 1L;

	private StoredProcedureExceptionType type;
	
	public enum StoredProcedureExceptionType {
		STOREDPROCEDURE_CANNOT_EXECUTE;

		public StoredProcedureException createException(String ... parameters){
			return new StoredProcedureException(this, parameters);
		}
	}

	public StoredProcedureException(StoredProcedureExceptionType type, String ... parameters) {
		this.type = type;
		this.parameters = parameters;
	}

	public StoredProcedureExceptionType getExceptionType() {
		return this.type;
	}
	
	public String getExceptionTypeText() {
		return this.type.toString();
	}
}
