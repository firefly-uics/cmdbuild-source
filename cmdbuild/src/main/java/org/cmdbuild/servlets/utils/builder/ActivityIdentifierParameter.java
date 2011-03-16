package org.cmdbuild.servlets.utils.builder;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.servlets.json.management.ActivityIdentifier;

public class ActivityIdentifierParameter extends
		AbstractParameterBuilder<ActivityIdentifier> {

	public ActivityIdentifier build(HttpServletRequest r) throws Exception {
		String procInstId = parameter("ProcessInstanceId", r);
		String workItemId = parameter("WorkItemId", r);
		return new ActivityIdentifier(procInstId,workItemId);
	}
}
