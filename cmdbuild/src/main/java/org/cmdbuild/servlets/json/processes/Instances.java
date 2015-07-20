package org.cmdbuild.servlets.json.processes;

import static org.cmdbuild.services.json.dto.JsonResponse.success;
import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVITY_INSTANCE_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.PROCESS_INSTANCE_ID;

import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.utils.Parameter;

public class Instances extends JSONBaseWithSpringContext {

	@JSONExported
	public JsonResponse lock( //
			@Parameter(value = PROCESS_INSTANCE_ID) final Long instanceId, //
			@Parameter(value = ACTIVITY_INSTANCE_ID) final String activityId //
	) {
		lockLogic().lockActivity(instanceId, activityId);
		return success();
	}

	@JSONExported
	public JsonResponse unlock( //
			@Parameter(value = PROCESS_INSTANCE_ID) final Long instanceId, //
			@Parameter(value = ACTIVITY_INSTANCE_ID) final String activityId //
	) {
		lockLogic().unlockActivity(instanceId, activityId);
		return success();
	}

	@Admin
	@JSONExported
	public JsonResponse unlockAll() {
		lockLogic().unlockAllActivities();
		return success();
	}

}
