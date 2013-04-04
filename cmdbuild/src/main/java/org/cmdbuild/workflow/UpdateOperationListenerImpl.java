package org.cmdbuild.workflow;

import org.cmdbuild.workflow.event.WorkflowEventManager;
import org.cmdbuild.workflow.service.AbstractSharkService.UpdateOperationListener;

/**
 * This implementation is tightly bound to how the {@link WorkflowEngineWrapper}
 * works. If lets the engine save the newly created process instance after it
 * killed the activity instances that it does not want.
 */
public class UpdateOperationListenerImpl implements UpdateOperationListener {

	private final WorkflowEventManager workflowEventManager;

	public UpdateOperationListenerImpl(final WorkflowEventManager workflowEventManager) {
		this.workflowEventManager = workflowEventManager;
	}

	@Override
	public void processInstanceStarted(int sessionId) {
		workflowEventManager.purgeEvents(sessionId);
	}

	@Override
	public void processInstanceAborted(int sessionId) throws CMWorkflowException {
		workflowEventManager.processEvents(sessionId);
	}

	@Override
	public void processInstanceSuspended(int sessionId) throws CMWorkflowException {
		workflowEventManager.processEvents(sessionId);
	}

	@Override
	public void processInstanceResumed(int sessionId) throws CMWorkflowException {
		workflowEventManager.processEvents(sessionId);
	}

	@Override
	public void activityInstanceAborted(int sessionId) {
		workflowEventManager.purgeEvents(sessionId);
	}

	@Override
	public void activityInstanceAdvanced(int sessionId) throws CMWorkflowException {
		workflowEventManager.processEvents(sessionId);
	}

	@Override
	public void abortedOperation(int sessionId) {
		workflowEventManager.purgeEvents(sessionId);
	}

}
