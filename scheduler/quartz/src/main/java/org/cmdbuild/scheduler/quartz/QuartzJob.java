package org.cmdbuild.scheduler.quartz;

import org.cmdbuild.scheduler.SchedulerJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class QuartzJob implements org.quartz.Job {

	private static final String JOB = "job";

	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		final JobDataMap data = context.getJobDetail().getJobDataMap();
		final SchedulerJob job = (SchedulerJob) data.get(JOB);
		job.execute();
	}

	public static JobDetail createJobDetail(final SchedulerJob job) {
		final JobDetail jobDetail = new JobDetail(job.getName(), null, QuartzJob.class);
		final JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(JOB, job);
		jobDetail.setJobDataMap(jobDataMap);
		return jobDetail;
	}

}
