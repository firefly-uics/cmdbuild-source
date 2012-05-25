package org.cmdbuild.workflow;

public interface CMEventManager {

	void processStarted(String processDefinitionId);

	void processClosed(String processDefinitionId);

	void processSuspended(String processDefinitionId);

	void processResumed(String processDefinitionId);

	void activityStarted(String activityDefinitionId);

	void activityClosed(String activityDefinitionId);

}
