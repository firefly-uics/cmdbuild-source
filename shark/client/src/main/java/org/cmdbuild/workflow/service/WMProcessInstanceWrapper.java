package org.cmdbuild.workflow.service;

import org.enhydra.shark.api.client.wfmc.wapi.WMProcessInstance;
import org.enhydra.shark.api.client.wfmc.wapi.WMProcessInstanceState;

public class WMProcessInstanceWrapper implements WSProcessInstInfo {

	final WMProcessInstance inner;

	WMProcessInstanceWrapper(final WMProcessInstance processInstance) {
		this.inner = processInstance;
	}

	@Override
	public String getProcessDefinitionId() {
		return inner.getProcessDefinitionId();
	}

	@Override
	public String getProcessInstanceId() {
		return inner.getId();
	}

	@Override
	public WSProcessInstanceState getStatus() {
		final WMProcessInstanceState state = inner.getState();
		if (state == null) {
			// We have no control over this field, so it's
			// best to assume that it might be null.
			return WSProcessInstanceState.UNSUPPORTED;
		}
		switch(state.value()) {
		case WMProcessInstanceState.OPEN_RUNNING_INT:
			return WSProcessInstanceState.OPEN;
		case WMProcessInstanceState.CLOSED_COMPLETED_INT:
			return WSProcessInstanceState.COMPLETED;
		case WMProcessInstanceState.CLOSED_ABORTED_INT:
			return WSProcessInstanceState.ABORTED;
		case WMProcessInstanceState.CLOSED_TERMINATED_INT:
			return WSProcessInstanceState.TERMINATED;
		case WMProcessInstanceState.OPEN_NOTRUNNING_SUSPENDED_INT:
			return WSProcessInstanceState.SUSPENDED;
		case WMProcessInstanceState.OPEN_NOTRUNNING_NOTSTARTED_INT:
		default:
			return WSProcessInstanceState.UNSUPPORTED;
		}
	}
}
