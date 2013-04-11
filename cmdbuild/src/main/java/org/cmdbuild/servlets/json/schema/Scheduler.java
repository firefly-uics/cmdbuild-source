package org.cmdbuild.servlets.json.schema;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.common.annotations.CheckIntegration;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.logic.scheduler.SchedulerLogic;
import org.cmdbuild.logic.scheduler.SchedulerLogic.ScheduledJob;
import org.cmdbuild.logic.scheduler.SchedulerLogic.ScheduledJobBuilder;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Scheduler extends JSONBaseWithSpringContext {

	/**
	 * TODO: only processType name is needed
	 */
	@CheckIntegration
	@Admin
	@JSONExported
	public JSONObject listProcessJobs(final ProcessType processType) throws JSONException {
		final Iterable<ScheduledJob> jobs = schedulerLogic().findJobsByDetail(processType.getName());
		return serializeScheduledJobs(jobs);
	}

	/**
	 * TODO: only processType name is needed
	 */
	@CheckIntegration
	@Admin
	@JSONExported
	@Transacted
	public JSONObject addProcessJob(final ProcessType processType,
			@Parameter("jobDescription") final String jobDescription,
			@Parameter("cronExpression") final String cronExpression,
			@Parameter(value = "jobParameters", required = false) final JSONObject jsonParameters) throws JSONException {
		final ScheduledJob scheduledJob = ScheduledJobBuilder.newScheduledJob() //
				.withDescription(jobDescription) //
				.withDetail(processType.getName()) //
				.withParams(convertJsonParams(jsonParameters)) //
				.withCronExpression(addSecondsField(cronExpression)) //
				.build();
		final ScheduledJob createdJob = schedulerLogic().createAndStart(scheduledJob);
		return serializeScheduledJob(createdJob);
	}

	@Admin
	@JSONExported
	@Transacted
	public JSONObject modifyJob(@Parameter("jobId") final Long jobId,
			@Parameter("jobDescription") final String jobDescription,
			@Parameter("cronExpression") final String cronExpression,
			@Parameter(value = "jobParameters", required = false) final JSONObject jsonParameters) throws JSONException {
		final SchedulerLogic schedulerLogic = schedulerLogic();
		final ScheduledJob oldJob = schedulerLogic.findJobById(jobId);
		final ScheduledJob updatedJob = ScheduledJobBuilder.newScheduledJob() //
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
	@Transacted
	public void deleteJob(@Parameter("jobId") final Long jobId) throws JSONException {
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
