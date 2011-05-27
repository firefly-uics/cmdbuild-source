package org.cmdbuild.shark.toolagent;

import org.cmdbuild.shark.util.CmdbuildUtils;
import org.enhydra.shark.Shark;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
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

/**
 * Update a workflow card<br/>
 * Parameters:
 * 1) ProcessInstanceId:string
 * 2...n-1) Name:Object (variables to be passed to the workflow)
 * n) Out:bool, true if the process was updated.
 * 
 * To complete the current activity, an extended attribute "Complete" (with value "1") must be used
 *
 */
public class ProcessUpdateToolAgent extends AbstractConditionalToolAgent {

	@Override
	protected void innerInvoke(WMSessionHandle shandle, long handle,
			WMEntity appInfo, WMEntity toolInfo, String applicationName,
			String procInstId, String assId, AppParameter[] parameters,
			Integer appMode) throws ApplicationNotStarted,
			ApplicationNotDefined, ApplicationBusy, ToolAgentGeneralException {
		try {
			WAPI wapi = getWapiConnection();
			WMWorkItem workItem = CmdbuildUtils.getActiveWorkItem(shandle, wapi, getTargetProcessParameter());

			openWorkItem(shandle, wapi, workItem);
			updateWorkItem(shandle, wapi, workItem, parameters);
			if (toBeCompleted()) {
				completeWorkItem(shandle, wapi, workItem);
			}
		} catch (Exception e) {
			throw new ToolAgentGeneralException(e);
		}
	}

	private void openWorkItem(WMSessionHandle shandle, WAPI wapi,
			WMWorkItem workItem) throws Exception {
		wapi.changeWorkItemState(shandle, workItem.getProcessInstanceId(), workItem.getId(), WMWorkItemState.OPEN_RUNNING);
	}

	private void updateWorkItem(WMSessionHandle shandle, WAPI wapi,
			WMWorkItem workItem, AppParameter[] parameters) throws Exception {
		for (int i=2; i<parameters.length; ++i) {
			AppParameter param = parameters[i];
			if (param.the_mode.equals("IN")) {
				wapi.assignWorkItemAttribute(shandle, workItem.getProcessInstanceId(), workItem.getId(), param.the_formal_name, param.the_value);
			}
		}
	}

	private void completeWorkItem(WMSessionHandle shandle, WAPI wapi,
			WMWorkItem workItem) throws Exception {
		wapi.changeWorkItemState(shandle, workItem.getProcessInstanceId(), workItem.getId(), WMWorkItemState.CLOSED_COMPLETED);
	}

	private String getTargetProcessParameter() {
		return get(parameters, 1);
	}

	private boolean toBeCompleted() throws ToolAgentGeneralException {
		ExtendedAttributes eas;
		try {
			eas = this.readParamsFromExtAttributes((String)parameters[0].the_value);
			if (eas.containsElement("Complete")) {
				return eas.getFirstExtendedAttributeForName("Complete").getVValue().equals("1");
			} else {
				return false;
			}
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
