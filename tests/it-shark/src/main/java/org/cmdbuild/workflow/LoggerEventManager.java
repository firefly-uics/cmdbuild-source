package org.cmdbuild.workflow;

import static java.lang.String.format;

import org.enhydra.shark.api.internal.working.CallbackUtilities;

public class LoggerEventManager implements CMEventManager {

	private final CallbackUtilities cus;

	public LoggerEventManager(final CallbackUtilities cus) {
		this.cus = cus;
	}

	@Override
	public void activityClosed(final String activityDefinitionId) {
		logWithId("activityClosed", activityDefinitionId);
	}

	@Override
	public void activityStarted(final String activityDefinitionId) {
		logWithId("activityStarted", activityDefinitionId);
	}

	@Override
	public void processClosed(final String processDefinitionId) {
		logWithId("processClosed", processDefinitionId);
	}

	@Override
	public void processResumed(final String processDefinitionId) {
		logWithId("processResumed", processDefinitionId);
	}

	@Override
	public void processStarted(final String processDefinitionId) {
		logWithId("processStarted", processDefinitionId);
	}

	@Override
	public void processSuspended(final String processDefinitionId) {
		logWithId("processSuspended", processDefinitionId);
	}

	private void logWithId(final String message, final String id) {
		cus.info(TestLoggerConstants.UNUSED_SHANDLE, TestLoggerConstants.LOGGER_CATEGORY, format("%s: %s", message, id));
	}

}
