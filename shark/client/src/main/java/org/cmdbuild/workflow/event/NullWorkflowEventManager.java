package org.cmdbuild.workflow.event;

public class NullWorkflowEventManager implements WorkflowEventManager {

	@Override
	public void pushEvent(final int sessionId, final WorkflowEvent event) {
	}

	@Override
	public void processEvents(final int sessionId) {
	}

	@Override
	public void purgeEvents(final int sessionId) {
	}

}
