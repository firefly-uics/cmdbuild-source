package org.cmdbuild.workflow.extattr;

import java.util.Map;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.workflow.operation.ActivityDO;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmc.wapi.WMWorkItem;
import org.enhydra.shark.client.utilities.SharkWSFactory;

public class Calendar extends AbstractFilteredExtendedAttribute {

	public String extendedAttributeName() {
		return "calendar";
	}

	@Override
	protected void doConfigure(WMSessionHandle handle, UserContext userCtx,
			SharkWSFactory factory, WMWorkItem workItem, ActivityDO activityDO,
			Map<String, Object> processVars,
			Map<String, Object> currentOutValues) {
	}
}
