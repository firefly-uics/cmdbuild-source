package org.cmdbuild.servlets.json.schema;

import static org.cmdbuild.servlets.json.ComunicationConstants.CLASS_NAME;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.logic.scheduler.DefaultScheduledJob;
import org.cmdbuild.logic.scheduler.SchedulerLogic;
import org.cmdbuild.logic.scheduler.SchedulerLogic.ScheduledJob;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Scheduler extends JSONBaseWithSpringContext {

	@Admin
	@JSONExported
	public JSONObject listProcessJobs( //
			@Parameter(CLASS_NAME) final String className //
	) throws JSONException {
		final Iterable<ScheduledJob> jobs = schedulerLogic().findJobsByDetail(className);
		return serializeScheduledJobs(jobs);
	}

	@Admin
	@JSONExported
	public JSONObject addProcessJob( //
			@Parameter(CLASS_NAME) final String className, //
			@Parameter("jobDescription") final String jobDescription, //
			@Parameter("cronExpression") final String cronExpression, //
			@Parameter(value = "jobParameters", required = false) final JSONObject jsonParameters //
	) throws JSONException {
		final ScheduledJob scheduledJob = DefaultScheduledJob.newRunningWorkflowJob() //
				.withDescription(jobDescription) //
				.withDetail(className) //
				.withParams(convertJsonParams(jsonParameters)) //
				.withCronExpression(addSecondsField(cronExpression)) //
				.build();
		final ScheduledJob createdJob = schedulerLogic().createAndStart(scheduledJob);
		return serializeScheduledJob(createdJob);
	}

	@Admin
	@JSONExported
	public JSONObject modifyJob( //
			@Parameter("jobId") final Long jobId, //
			@Parameter("jobDescription") final String jobDescription, //
			@Parameter("cronExpression") final String cronExpression, //
			@Parameter(value = "jobParameters", required = false) final JSONObject jsonParameters //
	) throws JSONException {
		final SchedulerLogic schedulerLogic = schedulerLogic();
		final ScheduledJob oldJob = schedulerLogic.findJobById(jobId);
		final ScheduledJob updatedJob = DefaultScheduledJob.newRunningWorkflowJob() //
				.withDetail(oldJob.getDetail()) //
				.withId(jobId) //
				.withDescription(jobDescription) //
				.withCronExpression(addSecondsField(cronExpression)) //
				.withParams(convertJsonParams(jsonParameters)) //
				.build();
		schedulerLogic.update(updatedJob);
		return serializeScheduledJob(updatedJob);
	}

	@Admin
	@JSONExported
	public void deleteJob( //
			@Parameter("jobId") final Long jobId //
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

	private JSONObject serializeScheduledJobs(final Iterable<ScheduledJob> jobsToSerialize) throws JSONException {
		final JSONObject response = new JSONObject();
		final JSONArray jobList = new JSONArray();
		for (final ScheduledJob job : jobsToSerialize) {
			jobList.put(serializeScheduledJob(job));
		}
		response.put("rows", jobList);
		return response;
	}

	private JSONObject serializeScheduledJob(final ScheduledJob scheduledJob) throws JSONException {
		final JSONObject serializedJob = new JSONObject();
		serializedJob.put("description", scheduledJob.getDescription());
		final Map<String, String> params = scheduledJob.getParams();
		final JSONObject jsonParams = new JSONObject();
		for (final String key : params.keySet()) {
			jsonParams.put(key, params.get(key));
		}
		serializedJob.put("params", jsonParams);
		serializedJob.put("cronExpression", removeSecondsField(scheduledJob.getCronExpression()));
		serializedJob.put("id", scheduledJob.getId());
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
