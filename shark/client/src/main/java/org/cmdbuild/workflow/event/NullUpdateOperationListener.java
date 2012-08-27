package org.cmdbuild.workflow.event;

import org.cmdbuild.workflow.service.AbstractSharkService.UpdateOperationListener;

public class NullUpdateOperationListener implements UpdateOperationListener {

	@Override
	public void processInstanceStarted(int sessionId) {
	}

	@Override
	public void processInstanceAborted(int sessionId) {
	}

	@Override
	public void processInstanceSuspended(int sessionId) {
	}

	@Override
	public void processInstanceResumed(int sessionId) {
	}

	@Override
	public void activityInstanceAborted(int sessionId) {
	}

	@Override
	public void activityInstanceAdvanced(int sessionId) {
	}

	@Override
	public void abortedOperation(int sessionId) {
	}


}
