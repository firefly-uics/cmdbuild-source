package org.cmdbuild.servlets.resource.shark;

import org.cmdbuild.logger.Log;
import org.cmdbuild.services.WorkflowService;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.resource.RESTExported;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.workflow.SharkWSFacade;
import org.cmdbuild.workflow.SharkWSFacade.SharkEvent;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmc.wapi.WMWorkItem;
import org.enhydra.shark.client.utilities.SharkWSFactory;

public class SuspendProcess extends AbstractSharkResource {

	public String baseURI() {
		return "suspendprocess";
	}
	
	@RESTExported(httpMethod=RESTExported.RestMethod.POST)
	public Boolean suspendProcess(
			@Parameter("processinstanceid") final String procInstId
			) throws Exception {
		SharkWSFacade.SharkEventListener listener = new SharkWSFacade.OneShotSharkEventListener() {
			public void handle(SharkEvent evtType, UserContext userCtx,
					SharkWSFacade facade, WMSessionHandle handle,
					SharkWSFactory factory, WorkflowService service,
					Object... vars) {
				if(SharkWSFacade.SharkEvent.ACTIVITY_CLOSED.equals(evtType) &&
						((WMWorkItem)vars[0]).getProcessInstanceId().equals(procInstId)) {
					Log.WORKFLOW.debug("suspending process: " + procInstId);
					facade.suspendProcess(handle, userCtx, factory, procInstId);
				}
			}
		};
		WorkflowService.getInstance().addSharkEventListener(listener);
		return true;
	}

}
