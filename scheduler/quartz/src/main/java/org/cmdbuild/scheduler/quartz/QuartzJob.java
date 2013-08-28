package org.cmdbuild.scheduler.quartz;

import org.cmdbuild.scheduler.SchedulerJob;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

public class QuartzJob implements org.quartz.Job {

	private static final String JOB = "job";

	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		final JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
		final SchedulerJob jobScheduler = SchedulerJob.class.cast(jobDataMap.get(JOB));
		jobScheduler.execute();
	}

	public static JobDetail createJobDetail(final SchedulerJob schedulerJob) {
		final JobDataMap jobDataMap = new JobDataMap();
		jobDataMap.put(JOB, schedulerJob);
		return JobBuilder.newJob(QuartzJob.class) //
				.withIdentity(schedulerJob.getName()) //
				.setJobData(jobDataMap) //
				.build();
	}

	public static JobKey createJobKey(final SchedulerJob job) {
		return JobKey.jobKey(job.getName());
	}

}
