package org.cmdbuild.servlets.json.management;

import java.util.Map;

import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.workflow.operation.SharkFacade;
import org.json.JSONException;
import org.json.JSONObject;

public class ModWorkflow extends JSONBase {

	/**
	 * Abort the process which holds the activity
	 * 
	 * @param params
	 * @return
	 * @throws JSONException
	 */
	@JSONExported
	public JSONObject abortProcess(JSONObject serializer, SharkFacade mngt, ActivityIdentifier ai,
			Map<String, String> params) throws JSONException {
		serializer.put("success", mngt.abortProcess(ai.processInstanceId, ai.workItemId));
		return serializer;
	}

}
