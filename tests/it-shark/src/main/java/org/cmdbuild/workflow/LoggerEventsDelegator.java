package org.cmdbuild.workflow;

public class LoggerEventsDelegator extends SharkEventsDelegator {

	private static final CMEventManager eventManager = new LoggerEventManager();

	public LoggerEventsDelegator() {
		super(eventManager);
	}

}
