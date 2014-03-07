package org.cmdbuild.services.scheduler.quartz;

import org.cmdbuild.services.scheduler.job.CMJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class QuartzJob implements org.quartz.Job {

	static final String JOB = "job";

	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		final JobDataMap data = context.getJobDetail().getJobDataMap();
		final CMJob job = (CMJob) data.get(JOB);
		job.execute();
	}

	public static JobDetail createJobDetail(final CMJob job) {
		final JobDetail jobDetail = new JobDetail(job.getName(), null, QuartzJob.class);
		final JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(JOB, job);
		jobDetail.setJobDataMap(jobDataMap);
		return jobDetail;
	}
}
