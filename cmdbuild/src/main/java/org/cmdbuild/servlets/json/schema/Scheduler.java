package org.cmdbuild.servlets.json.schema;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.common.annotations.CheckIntegration;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.scheduler.SchedulerLogic;
import org.cmdbuild.logic.scheduler.SchedulerLogic.ScheduledJob;
import org.cmdbuild.logic.scheduler.SchedulerLogic.ScheduledJobBuilder;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Scheduler extends JSONBase {

	/**
	 * TODO: only processType name is needed
	 */
	@CheckIntegration
	@Admin
	@JSONExported
	public JSONObject listProcessJobs(final ProcessType processType) throws JSONException {
		final SchedulerLogic schedulerLogic = TemporaryObjectsBeforeSpringDI.getSchedulerLogic();
		Iterable<ScheduledJob> jobs = schedulerLogic.findJobsByDetail(processType.getName());
		return serializeScheduledJobs(jobs);
	}

	/**
	 * TODO: only processType name is needed
	 */
	@CheckIntegration
	@Admin
	@JSONExported
	@Transacted
	public JSONObject addProcessJob(ProcessType processType, @Parameter("jobDescription") String jobDescription,
			@Parameter("cronExpression") String cronExpression,
			@Parameter(value = "jobParameters", required = false) JSONObject jsonParameters) throws JSONException {
		final SchedulerLogic schedulerLogic = TemporaryObjectsBeforeSpringDI.getSchedulerLogic();
		final ScheduledJob scheduledJob = ScheduledJobBuilder.newScheduledJob() //
				.withDescription(jobDescription) //
				.withDetail(processType.getName()) //
				.withParams(convertJsonParams(jsonParameters)) //
				.withCronExpression(addSecondsField(cronExpression)) //
				.build();
		final ScheduledJob createdJob = schedulerLogic.createAndStart(scheduledJob);
		return serializeScheduledJob(createdJob);
	}

	@Admin
	@JSONExported
	@Transacted
	public JSONObject modifyJob(@Parameter("jobId") Long jobId, @Parameter("jobDescription") String jobDescription,
			@Parameter("cronExpression") String cronExpression,
			@Parameter(value = "jobParameters", required = false) JSONObject jsonParameters) throws JSONException {

		final SchedulerLogic schedulerLogic = TemporaryObjectsBeforeSpringDI.getSchedulerLogic();
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
	public void deleteJob(@Parameter("jobId") Long jobId) throws JSONException {
		final SchedulerLogic schedulerLogic = TemporaryObjectsBeforeSpringDI.getSchedulerLogic();
		schedulerLogic.delete(jobId);
	}

	private Map<String, String> convertJsonParams(JSONObject jsonParameters) throws JSONException {
		Map<String, String> params = new HashMap<String, String>();
		if (jsonParameters != null && jsonParameters.length() > 0) {
			for (String key : JSONObject.getNames(jsonParameters)) {
				params.put(key, jsonParameters.getString(key));
			}
		}
		return params;
	}

	private JSONObject serializeScheduledJobs(Iterable<ScheduledJob> jobsToSerialize) throws JSONException {
		JSONObject response = new JSONObject();
		JSONArray jobList = new JSONArray();
		for (ScheduledJob job : jobsToSerialize) {
			jobList.put(serializeScheduledJob(job));
		}
		response.put("rows", jobList);
		return response;
	}

	private JSONObject serializeScheduledJob(ScheduledJob scheduledJob) throws JSONException {
		JSONObject serializedJob = new JSONObject();
		serializedJob.put("description", scheduledJob.getDescription());
		Map<String, String> params = scheduledJob.getParams();
		JSONObject jsonParams = new JSONObject();
		for (String key : params.keySet()) {
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
	private String removeSecondsField(String cronExpression) {
		return cronExpression.substring(2);
	}

	private String addSecondsField(String cronExpression) {
		return "0 " + cronExpression;
	}
};
