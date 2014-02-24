package org.cmdbuild.servlets.json.schema;

import static org.cmdbuild.servlets.json.ComunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.CRON_EXPRESSION;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.JOB_DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.JOB_ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.JOB_PARAMETERS;
import static org.cmdbuild.servlets.json.ComunicationConstants.PARAMS;
import static org.cmdbuild.servlets.json.ComunicationConstants.ROWS;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.logic.taskmanager.StartWorkflowTask;
import org.cmdbuild.model.scheduler.SchedulerJob;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Scheduler extends JSONBaseWithSpringContext {

	private static final String LEGACY_FIXED_LABEL_START_PROCESS = "StartProcess";

	@Admin
	@JSONExported
	public JSONObject listProcessJobs( //
			@Parameter(CLASS_NAME) final String className //
	) throws JSONException {
		final Iterable<SchedulerJob> jobs = schedulerLogic().findJobsByDetail(className);
		return serializeScheduledJobs(jobs);
	}

	@Admin
	@JSONExported
	public JsonResponse addProcessJob( //
			@Parameter(CLASS_NAME) final String className, //
			@Parameter(JOB_DESCRIPTION) final String jobDescription, //
			@Parameter(CRON_EXPRESSION) final String cronExpression, //
			@Parameter(value = JOB_PARAMETERS, required = false) final JSONObject jsonParameters //
	) throws JSONException {
		final StartWorkflowTask task = StartWorkflowTask.newInstance() //
				.withName(LEGACY_FIXED_LABEL_START_PROCESS) //
				.withDescription(jobDescription) //
				.withActiveStatus(true) //
				.withProcessClass(className) //
				.withCronExpression(addSecondsField(cronExpression)) //
				.withParameters(convertJsonParams(jsonParameters)) //
				.build();
		taskManagerLogic().add(task);
		return JsonResponse.success();
	}

	@Admin
	@JSONExported
	public JSONObject modifyJob( //
			@Parameter(JOB_ID) final Long jobId, //
			@Parameter(JOB_DESCRIPTION) final String jobDescription, //
			@Parameter(CRON_EXPRESSION) final String cronExpression, //
			@Parameter(value = JOB_PARAMETERS, required = false) final JSONObject jsonParameters //
	) throws JSONException {
		final SchedulerJob jobToBeUpdated = new SchedulerJob(jobId);
		jobToBeUpdated.setDescription(jobDescription);
		jobToBeUpdated.setCronExpression(addSecondsField(cronExpression));
		jobToBeUpdated.setLegacyParameters(convertJsonParams(jsonParameters));

		final SchedulerJob updatedJob = schedulerLogic().update(jobToBeUpdated);
		return serializeScheduledJob(updatedJob);
	}

	@Admin
	@JSONExported
	public void deleteJob( //
			@Parameter(JOB_ID) final Long jobId //
	) throws JSONException {
		schedulerLogic().delete(jobId);
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

	private JSONObject serializeScheduledJobs(final Iterable<SchedulerJob> jobsToSerialize) throws JSONException {
		final JSONObject response = new JSONObject();
		final JSONArray jobList = new JSONArray();
		for (final SchedulerJob job : jobsToSerialize) {
			jobList.put(serializeScheduledJob(job));
		}
		response.put(ROWS, jobList);
		return response;
	}

	private JSONObject serializeScheduledJob(final SchedulerJob scheduledJob) throws JSONException {
		final JSONObject serializedJob = new JSONObject();
		serializedJob.put(DESCRIPTION, scheduledJob.getDescription());
		final Map<String, String> params = scheduledJob.getLegacyParameters();
		final JSONObject jsonParams = new JSONObject();
		for (final String key : params.keySet()) {
			jsonParams.put(key, params.get(key));
		}
		serializedJob.put(PARAMS, jsonParams);
		serializedJob.put(CRON_EXPRESSION, removeSecondsField(scheduledJob.getCronExpression()));
		serializedJob.put(ID, scheduledJob.getIdentifier());
		return serializedJob;
	}

	/*
	 * the js interface does not handle the seconds
	 */
	private String removeSecondsField(final String cronExpression) {
		return cronExpression.substring(2);
	}

	private String addSecondsField(final String cronExpression) {
		return "0 " + cronExpression;
	}
};
