package org.cmdbuild.servlets.json.schema;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.services.meta.MetadataService;
import org.cmdbuild.services.scheduler.SchedulerService;
import org.cmdbuild.services.scheduler.job.Job;
import org.cmdbuild.services.scheduler.job.JobCard;
import org.cmdbuild.services.scheduler.quartz.QuartzScheduler;
import org.cmdbuild.services.scheduler.trigger.JobTrigger;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Scheduler extends JSONBase {

	@OldDao
	@Admin
	@JSONExported
	public JSONObject listProcessJobs(ProcessType processType) throws JSONException {
		return serializeJobCardList(JobCard.allForDetail(MetadataService.getSchemaFullName(processType)));
	}

	@OldDao
	@Admin
	@JSONExported
	@Transacted
	public JSONObject addProcessJob(ProcessType processType, @Parameter("jobDescription") String jobDescription,
			@Parameter("cronExpression") String cronExpression,
			@Parameter(value = "jobParameters", required = false) JSONObject jsonParameters) throws JSONException {
		SchedulerService scheduler = new QuartzScheduler(); // TODO
		JobCard jobCard = new JobCard();
		jobCard.setDetail(MetadataService.getSchemaFullName(processType));
		jobCard.setDescription(jobDescription);
		jobCard.setParams(convertJsonParams(jsonParameters));
		jobCard.setCronExpression(addSecondsField(cronExpression));
		jobCard.save();
		scheduler.addJob(jobCard.createJob(), jobCard.createJobTrigger());
		return serializeJobCard(jobCard);
	}

	@OldDao
	@Admin
	@JSONExported
	@Transacted
	public JSONObject modifyJob(@Parameter("jobId") int jobId, @Parameter("jobDescription") String jobDescription,
			@Parameter("cronExpression") String cronExpression,
			@Parameter(value = "jobParameters", required = false) JSONObject jsonParameters) throws JSONException {
		SchedulerService scheduler = new QuartzScheduler(); // TODO
		JobCard jobCard = new JobCard(jobId);

		// save an old copy of the job for rollback
		Job oldJob = jobCard.createJob();
		JobTrigger oldJobTrigger = jobCard.createJobTrigger();

		scheduler.removeJob(jobCard.createJob());
		try {
			jobCard.setDescription(jobDescription);
			jobCard.setParams(convertJsonParams(jsonParameters));
			jobCard.setCronExpression(addSecondsField(cronExpression));
			jobCard.save();
			scheduler.addJob(jobCard.createJob(), jobCard.createJobTrigger());
		} catch (CMDBException e) {
			scheduler.addJob(oldJob, oldJobTrigger);
			throw e;
		}
		return serializeJobCard(jobCard);
	}

	@OldDao
	@Admin
	@JSONExported
	@Transacted
	public void deleteJob(@Parameter("jobId") int jobId) throws JSONException {
		SchedulerService scheduler = new QuartzScheduler(); // TODO
		JobCard jobCard = new JobCard(jobId);
		jobCard.delete();
		scheduler.removeJob(jobCard.createJob());
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

	private JSONObject serializeJobCardList(Iterable<JobCard> jobCardList) throws JSONException {
		JSONObject response = new JSONObject();
		JSONArray jobList = new JSONArray();
		for (JobCard job : jobCardList) {
			jobList.put(serializeJobCard(job));
		}
		response.put("rows", jobList);
		return response;
	}

	private JSONObject serializeJobCard(JobCard jobCard) throws JSONException {
		JSONObject serializedJob = new JSONObject();
		serializedJob.put("description", jobCard.getDescription());
		Map<String, String> params = jobCard.getParams();
		JSONObject jsonParams = new JSONObject();
		for (String key : params.keySet()) {
			jsonParams.put(key, params.get(key));
		}
		serializedJob.put("params", jsonParams);
		serializedJob.put("cronExpression", removeSecondsField(jobCard.getCronExpression()));
		serializedJob.put("id", jobCard.getId());
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
