package utils;

import static java.lang.String.format;

import org.apache.log4j.Logger;
import org.cmdbuild.workflow.CMEventManager;

public class TestLoggerEventManager implements CMEventManager {

	private static final Logger logger = Logger.getLogger("IT");

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
		logger.info(format("%s: %s", message, id));
	}

}
