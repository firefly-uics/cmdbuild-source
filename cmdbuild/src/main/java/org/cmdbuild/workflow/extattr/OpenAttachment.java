package org.cmdbuild.workflow.extattr;

import java.util.Map;

import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.operation.ActivityDO;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmc.wapi.WMWorkItem;
import org.enhydra.shark.client.utilities.SharkWSFactory;
import org.json.JSONException;
import org.json.JSONObject;

public class OpenAttachment extends AbstractCmdbuildExtendedAttribute {

	@Override
	protected void doConfigure(WMSessionHandle handle, UserContext userCtx,
			SharkWSFactory factory, WMWorkItem workItem, ActivityDO activityDO,
			Map<String, Object> processVars,
			Map<String, Object> currentOutValues) {
	}

	@Override
	protected void addCustomParams(ActivityDO activityDO, JSONObject object,
			ExtendedAttributeConfigParams eacp) throws JSONException {
		object.put("labelId", "open_attachment");
	}

	public String extendedAttributeName() {
		return "openAttachment";
	}
	
}
