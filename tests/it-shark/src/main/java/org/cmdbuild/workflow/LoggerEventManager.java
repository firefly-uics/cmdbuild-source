package org.cmdbuild.workflow;

import static java.lang.String.format;

import org.enhydra.shark.api.internal.working.CallbackUtilities;

public class LoggerEventManager implements CMEventManager {

	public static final String ACTIVITY_STARTED_LOG = "activityStarted";
	public static final String ACTIVITY_CLOSED_LOG = "activityClosed";
	public static final String PROCESS_STARTED_LOG = "processStarted";
	public static final String PROCESS_CLOSED_LOG = "processClosed";
	public static final String PROCESS_SUSPENDED_LOG = "processSuspended";
	public static final String PROCESS_RESUMED_LOG = "processResumed";

	private final CallbackUtilities cus;

	public LoggerEventManager(final CallbackUtilities cus) {
		this.cus = cus;
	}

	@Override
	public void activityClosed(final String activityDefinitionId) {
		logWithId(ACTIVITY_CLOSED_LOG, activityDefinitionId);
	}

	@Override
	public void activityStarted(final String activityDefinitionId) {
		logWithId(ACTIVITY_STARTED_LOG, activityDefinitionId);
	}

	@Override
	public void processClosed(final String processDefinitionId) {
		logWithId(PROCESS_CLOSED_LOG, processDefinitionId);
	}

	@Override
	public void processResumed(final String processDefinitionId) {
		logWithId(PROCESS_RESUMED_LOG, processDefinitionId);
	}

	@Override
	public void processStarted(final String processDefinitionId) {
		logWithId(PROCESS_STARTED_LOG, processDefinitionId);
	}

	@Override
	public void processSuspended(final String processDefinitionId) {
		logWithId(PROCESS_SUSPENDED_LOG, processDefinitionId);
	}

	private void logWithId(final String message, final String id) {
		cus.info(TestLoggerConstants.UNUSED_SHANDLE, TestLoggerConstants.LOGGER_CATEGORY, format("%s: %s", message, id));
	}

}
