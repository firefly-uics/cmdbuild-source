package org.cmdbuild.services.scheduler.quartz;

import org.cmdbuild.services.scheduler.job.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class QuartzJob implements org.quartz.Job {

	static final String JOB = "job";

	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.getJobDetail().getJobDataMap();
		Job job = (Job) data.get(JOB);
		job.execute();
	}

	public static JobDetail createJobDetail(Job job) {
		JobDetail jobDetail = new JobDetail(job.getName(), null, QuartzJob.class);
		JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(JOB, job);
		jobDetail.setJobDataMap(jobDataMap);
		return jobDetail;
	}
}
