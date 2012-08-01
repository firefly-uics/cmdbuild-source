package org.cmdbuild.shark.toolagent;

import org.cmdbuild.shark.util.CmdbuildUtils;
import org.enhydra.shark.Shark;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMProcessInstanceState;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmc.wapi.WMWorkItem;
import org.enhydra.shark.api.client.wfmc.wapi.WMWorkItemState;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.enhydra.shark.api.internal.toolagent.ApplicationBusy;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotDefined;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotStarted;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;
import org.enhydra.shark.xpdl.elements.ExtendedAttributes;

public class ProcessChangeStateToolAgent extends AbstractConditionalToolAgent {

	@Override
	protected void innerInvoke(WMSessionHandle shandle, long handle,
			WMEntity appInfo, WMEntity toolInfo, String applicationName,
			String procInstId, String assId, AppParameter[] parameters,
			Integer appMode) throws ApplicationNotStarted,
			ApplicationNotDefined, ApplicationBusy, ToolAgentGeneralException {
		String targetProcessInstance = getTargetProcessParameter();
		WMProcessInstanceState newState = getNewStateParameter();

		if ("CURRENT".equalsIgnoreCase(targetProcessInstance)) {
			targetProcessInstance = selfSuspendProcessInstance(procInstId, targetProcessInstance, newState);
		} else {
			WAPI wapi = getWapiConnection();
			changeProcessInstanceState(shandle, wapi, targetProcessInstance, newState);
			if (toBeCompleted()) {
				completeProcess(shandle, wapi, targetProcessInstance);
			}
		}
	}

	private WMProcessInstanceState getNewStateParameter() throws ToolAgentGeneralException {
		String newStateStr = getStateStringParameter();
		return processStateFromString(newStateStr);
	}

	private String getStateStringParameter()
			throws ToolAgentGeneralException {
		String newStateStr = null;
		try {
			String extAttribs = get(parameters, 0);
			ExtendedAttributes eas = this.readParamsFromExtAttributes(extAttribs);
			newStateStr = eas.getFirstExtendedAttributeForName("State").getVValue();
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new ToolAgentGeneralException("Cannot read ProcessChangeToolAgent extended attributes");
		}
		return newStateStr;
	}

	private String getTargetProcessParameter() {
		return get(parameters, 1);
	}

	private boolean toBeCompleted() {
		if (parameters.length > 2) {
			return (getInt(parameters, 2) == 1);
		} else {
			return false;
		}
	}

	private WMProcessInstanceState processStateFromString(String stateString) throws ToolAgentGeneralException {
		if(stateString.equalsIgnoreCase("Suspend")) {
			return WMProcessInstanceState.OPEN_NOTRUNNING_SUSPENDED;
		} else if(stateString.equalsIgnoreCase("Resume")) {
			return WMProcessInstanceState.OPEN_RUNNING;
		} else {
			throw new ToolAgentGeneralException("Unrecognized state string: " + stateString);
		}
	}

	private String selfSuspendProcessInstance(String procInstId,
			String processToChange, WMProcessInstanceState newState)
			throws ToolAgentGeneralException {
		if (newState == WMProcessInstanceState.OPEN_NOTRUNNING_SUSPENDED) {
			System.out.println("Current process is to pause, deferred pause call");
			boolean done = false;
			try {
				done = CmdbuildUtils.getInstance().sendSuspendProcess(procInstId);
			} catch(Exception e) {
				e.printStackTrace();
				throw new ToolAgentGeneralException("Cannot change state of process: " + procInstId);
			}
			System.out.println("request done: " + done);
		} else {
			throw new ToolAgentGeneralException("Only SUSPEND state is supported for CURRENT processes! - " + newState.stringValue());
		}
		return processToChange;
	}

	private void changeProcessInstanceState(WMSessionHandle shandle, WAPI wapi,
			String processToChange, WMProcessInstanceState newState)
			throws ToolAgentGeneralException {
		try {
			wapi.changeProcessInstanceState(shandle, processToChange, newState);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ToolAgentGeneralException("Cannot change state of process: " + processToChange);
		}
	}

	private void completeProcess(WMSessionHandle shandle, WAPI wapi, String targetProcessInstanceId) throws ToolAgentGeneralException {
		try {
			WMWorkItem workItem = CmdbuildUtils.getActiveWorkItem(shandle, wapi, targetProcessInstanceId);
			wapi.changeWorkItemState(shandle, workItem.getProcessInstanceId(), workItem.getId(), WMWorkItemState.OPEN_RUNNING);
			wapi.changeWorkItemState(shandle, workItem.getProcessInstanceId(), workItem.getId(), WMWorkItemState.CLOSED_COMPLETED);
		} catch (Exception e) {
			throw new ToolAgentGeneralException(e);
		}
	}

	private WAPI getWapiConnection() throws ToolAgentGeneralException {
		try {
			return Shark.getInstance().getWAPIConnection();
		} catch (Exception e) {
			throw new ToolAgentGeneralException("Cannot obtain WAPI connection", e);
		}
	}
}
