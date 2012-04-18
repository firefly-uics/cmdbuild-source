package org.cmdbuild.shark.util;

import java.util.Map;

import org.enhydra.shark.Shark;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttribute;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttributeIterator;
import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmodel.WfProcess;
import org.enhydra.shark.api.client.wfservice.SharkConnection;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.client.wfservice.XPDLBrowser;
import org.enhydra.shark.api.internal.scripting.Evaluator;
import org.enhydra.shark.api.internal.scripting.ScriptingManager;
import org.enhydra.shark.scripting.StandardScriptingManager;
import org.enhydra.shark.utilities.WMEntityUtilities;

public class ActivityPerformerResolver {
	
	/*
	 * for performance reasons, maybe it's better to hold a short cache of the -say- last 50 user name resolved,
	 * because chance are that cmdbuild or some other integration system will needs to resolve the performer of
	 * the same activity more than once.
	 */

	@SuppressWarnings("unchecked")
	public String resolveArbitraryPerformer( 
			SharkConnection sconn, WMSessionHandle handle,
			String processInstanceId, String activityInstanceId,
			String expr) throws Exception {
		String out = "";
		
		Shark shark = Shark.getInstance();
		ScriptingManager scriptMngr = (ScriptingManager)shark.getPlugIn("ScriptingManager");
		Evaluator evaluator = scriptMngr.getEvaluator(handle, StandardScriptingManager.JAVA_LANGUAGE_SCRIPT);
		
		WfProcess wfProc = sconn.getProcess(processInstanceId);
		Map ctxt = wfProc.process_context();
		
		out = (String)evaluator.evaluateExpression(handle, processInstanceId, activityInstanceId, expr, ctxt, String.class);
		
		return out;
	}
	
	public String resolvePerformer(
			SharkConnection sconn,
			WMSessionHandle handle,
			String processInstanceId, String activityInstanceId
			) throws Exception {
		String nextExecutor = null;
		String perfOrId = null;
		XPDLBrowser browser = Shark.getInstance().getXPDLBrowser();

		WMEntity actEntity = Shark.getInstance().getAdminMisc().getActivityDefinitionInfo(
				handle, processInstanceId, activityInstanceId);
		WMFilter filter = new WMFilter("Name", WMFilter.EQ, "Performer");
		filter.setFilterType(XPDLBrowser.SIMPLE_TYPE_XPDL);
		WMAttributeIterator ei = browser.listAttributes(handle, actEntity, filter, true);
		WMAttribute out = (ei.hasNext()?ei.getArray()[0]:null);
		perfOrId = (String)out.getValue();
		
		WMEntity procEntity = Shark.getInstance().getAdminMisc().getProcessDefinitionInfo(handle, processInstanceId);
		WMEntity[] participants = WMEntityUtilities.getAllParticipants(handle, browser, procEntity);
		
		for(WMEntity participant : participants) {
			if(participant.getId().equals(perfOrId)) {
				nextExecutor = perfOrId;
			}
		}
		
		if(nextExecutor == null) {
			return resolveArbitraryPerformer(sconn,handle,processInstanceId,activityInstanceId,perfOrId);
		}
		return nextExecutor;
	}
}
