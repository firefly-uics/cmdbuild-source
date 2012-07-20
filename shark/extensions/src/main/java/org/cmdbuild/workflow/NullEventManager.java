package org.cmdbuild.workflow;

public class NullEventManager implements CMEventManager {

	@Override
	public void processStarted(final String processDefinitionId) {
	}

	@Override
	public void processClosed(final String processDefinitionId) {
	}

	@Override
	public void processSuspended(final String processDefinitionId) {
	}

	@Override
	public void processResumed(final String processDefinitionId) {
	}

	@Override
	public void activityStarted(final String activityDefinitionId) {
	}

	@Override
	public void activityClosed(final String activityDefinitionId) {
	}

	public void noActiveActivities() {
	}

}
