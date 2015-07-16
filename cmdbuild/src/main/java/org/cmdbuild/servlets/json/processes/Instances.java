package org.cmdbuild.servlets.json.processes;

import static org.cmdbuild.services.json.dto.JsonResponse.failure;
import static org.cmdbuild.services.json.dto.JsonResponse.success;
import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVITY_INSTANCE_ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.PROCESS_INSTANCE_ID;

import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.utils.Parameter;

public class Instances extends JSONBaseWithSpringContext {

	@JSONExported
	public JsonResponse lock( //
			@Parameter(value = PROCESS_INSTANCE_ID) final Long instanceId, //
			@Parameter(value = ACTIVITY_INSTANCE_ID) final Long activityId //
	) {
		JsonResponse response;
		try {
			userDataAccessLogic().lockActivity(instanceId, activityId);
			response = success();
		} catch (final Exception e) {
			if (e instanceof CMDBException) {
				notifier().warn(CMDBException.class.cast(e));
			}
			response = failure(e);
		}
		return response;
	}

	@JSONExported
	public JsonResponse unlock( //
			@Parameter(value = PROCESS_INSTANCE_ID) final Long instanceId, //
			@Parameter(value = ACTIVITY_INSTANCE_ID) final Long activityId //
	) {
		userDataAccessLogic().unlockActivity(instanceId, activityId);
		return success();
	}

	@Admin
	@JSONExported
	public JsonResponse unlockAll() {
		userDataAccessLogic().unlockAllActivities();
		return success();
	}

}
