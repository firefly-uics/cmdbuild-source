package org.cmdbuild.services.scheduler.quartz;

import org.cmdbuild.exception.SchedulerException.SchedulerExceptionType;
import org.cmdbuild.services.scheduler.SchedulerService;
import org.cmdbuild.services.scheduler.job.Job;
import org.cmdbuild.services.scheduler.trigger.JobTrigger;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzScheduler implements SchedulerService {

	private org.quartz.Scheduler scheduler;

	public QuartzScheduler() {
		try {
			scheduler = StdSchedulerFactory.getDefaultScheduler();
		} catch (org.quartz.SchedulerException e) {
			throw SchedulerExceptionType.SCHEDULER_INTERNAL_ERROR.createException(e.getMessage());
		}
	}

	public void addJob(Job job, JobTrigger trigger) {
		Trigger qTrigger = new QuartzTriggerFactory().create(trigger);
		JobDetail qJobDetail = QuartzJob.createJobDetail(job);
		try {
			scheduler.scheduleJob(qJobDetail, qTrigger);
		} catch (org.quartz.SchedulerException e) {
			throw SchedulerExceptionType.SCHEDULER_INTERNAL_ERROR.createException(e.getMessage());
		}
	}

	public void removeJob(Job job) {
		try {
			scheduler.deleteJob(job.getName(), null);
		} catch (org.quartz.SchedulerException e) {
			throw SchedulerExceptionType.SCHEDULER_INTERNAL_ERROR.createException(e.getMessage());
		}
	}

	public void start() {
		try {
			scheduler.start();
		} catch (org.quartz.SchedulerException e) {
			throw SchedulerExceptionType.SCHEDULER_INTERNAL_ERROR.createException(e.getMessage());
		}
	}

	public void stop() {
		try {
			if (!scheduler.isShutdown()) {
				scheduler.shutdown(true);
			}
		} catch (org.quartz.SchedulerException e) {
			throw SchedulerExceptionType.SCHEDULER_INTERNAL_ERROR.createException(e.getMessage());
		}
	}
}
