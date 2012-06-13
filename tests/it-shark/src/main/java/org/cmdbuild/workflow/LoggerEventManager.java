package org.cmdbuild.workflow;

import static java.lang.String.format;

import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

public class LoggerEventManager implements CMEventManager {

	private static final WMSessionHandle UNUSED_SHANDLE = null;
	private static final String LOGGER_CATEGORY = "IT";

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
		cus.info(UNUSED_SHANDLE, LOGGER_CATEGORY, format("%s: %s", message, id));
	}

}
