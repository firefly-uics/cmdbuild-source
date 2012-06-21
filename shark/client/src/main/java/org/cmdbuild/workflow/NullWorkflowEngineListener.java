package org.cmdbuild.workflow;

public class NullWorkflowEngineListener implements CMWorkflowEngineListener {

	@Override
	public void syncStarted() {
	}

	@Override
	public void syncProcessStarted(CMProcessClass processClass) {
	}

	@Override
	public void syncProcessInstanceNotFound(CMProcessInstance processInstance) {
	}

	@Override
	public void syncProcessInstanceFound(CMProcessInstance processInstance) {
	}

	@Override
	public void syncFinished() {
	}

}
