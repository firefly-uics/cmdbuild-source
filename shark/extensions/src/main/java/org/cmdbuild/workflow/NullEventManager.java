package org.cmdbuild.workflow;

public class NullEventManager implements CMEventManager {

	public void processStarted(final String processDefinitionId) {
	}

	public void processClosed(final String processDefinitionId) {
	}

	public void processSuspended(final String processDefinitionId) {
	}

	public void processResumed(final String processDefinitionId) {
	}

	public void activityStarted(final String activityDefinitionId) {
	}

	public void activityClosed(final String activityDefinitionId) {
	}

}
