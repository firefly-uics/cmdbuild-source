package org.cmdbuild.exception;

public class SchedulerException extends CMDBException {

	private static final long serialVersionUID = 1L;
	
	private SchedulerExceptionType type;
	
	public enum SchedulerExceptionType {
		SCHEDULER_INTERNAL_ERROR,
		ILLEGAL_CRON_EXPRESSION; // failed expression

		public SchedulerException createException(String ... parameters){
			return new SchedulerException(this, parameters);
		}
	}

	private SchedulerException(SchedulerExceptionType type, String ... parameters) {
		this.type = type;
		this.parameters = parameters;
	}

	public SchedulerExceptionType getExceptionType() {
		return this.type;
	}
	
	public String getExceptionTypeText() {
		return this.type.toString();
	}
}
