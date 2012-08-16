package org.cmdbuild.workflow;

public interface CMEventManager {

	interface ProcessInstance {
		String getProcessDefinitionId();

		String getProcessInstanceId();
	}

	interface ActivityInstance extends ProcessInstance {
		String getActivityDefinitionId();

		String getActivityInstanceId();
	}

	void processStarted(ProcessInstance processInstance);

	void processClosed(ProcessInstance processInstance);

	void processSuspended(ProcessInstance processInstance);

	void processResumed(ProcessInstance processInstance);

	void activityStarted(ActivityInstance activityInstance);

	void activityClosed(ActivityInstance activityInstance);

}
