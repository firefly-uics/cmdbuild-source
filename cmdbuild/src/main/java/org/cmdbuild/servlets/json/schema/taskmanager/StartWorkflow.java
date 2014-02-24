package org.cmdbuild.servlets.json.schema.taskmanager;

import static org.cmdbuild.servlets.json.ComunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.CRON_EXPRESSION;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.PARAMS;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.logic.taskmanager.StartWorkflowTask;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONException;
import org.json.JSONObject;

public class StartWorkflow extends JSONBaseWithSpringContext {

	@Admin
	@JSONExported
	public JsonResponse create( //
			@Parameter(CLASS_NAME) final String className, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(CRON_EXPRESSION) final String cronExpression, //
			@Parameter(value = PARAMS, required = false) final JSONObject jsonParameters //
	) throws JSONException {
		final StartWorkflowTask task = StartWorkflowTask.newInstance() //
				.withDescription(description) //
				.withActiveStatus(true) //
				.withProcessClass(className) //
				.withCronExpression(addSecondsField(cronExpression)) //
				.withParameters(convertJsonParams(jsonParameters)) //
				.build();
		final Long id = taskManagerLogic().create(task);
		return JsonResponse.success(id);
	}

	@JSONExported
	public JsonResponse read( //
			@Parameter(value = ID) final String id //
	) {
		// TODO
		// JsonResponse.success(from(tasks) //
		// .transform(TASK_TO_JSON_TASK));
		return null;
	}

	@Admin
	@JSONExported
	public JsonResponse update( //
			@Parameter(ID) final Long id, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(CRON_EXPRESSION) final String cronExpression, //
			@Parameter(value = PARAMS, required = false) final JSONObject jsonParameters //
	) throws JSONException {
		final StartWorkflowTask task = StartWorkflowTask.newInstance() //
				.withId(id) //
				.withDescription(description) //
				.withCronExpression(addSecondsField(cronExpression)) //
				.withParameters(convertJsonParams(jsonParameters)) //
				.build();
		taskManagerLogic().update(task);
		return JsonResponse.success();
	}

	@Admin
	@JSONExported
	public void delete( //
			@Parameter(ID) final Long id //
	) throws JSONException {
		final StartWorkflowTask task = StartWorkflowTask.newInstance() //
				.withId(id) //
				.build();
		taskManagerLogic().delete(task);
	}

	/*
	 * Utilities
	 */

	private String addSecondsField(final String cronExpression) {
		return "0 " + cronExpression;
	}

	private Map<String, String> convertJsonParams(final JSONObject jsonParameters) throws JSONException {
		final Map<String, String> params = new HashMap<String, String>();
		if (jsonParameters != null && jsonParameters.length() > 0) {
			for (final String key : JSONObject.getNames(jsonParameters)) {
				params.put(key, jsonParameters.getString(key));
			}
		}
		return params;
	}

}
