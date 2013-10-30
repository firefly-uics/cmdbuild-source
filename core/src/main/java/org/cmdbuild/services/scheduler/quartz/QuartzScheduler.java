package org.cmdbuild.services.scheduler.quartz;

import org.cmdbuild.exception.SchedulerException.SchedulerExceptionType;
import org.cmdbuild.services.scheduler.SchedulerService;
import org.cmdbuild.services.scheduler.job.CMJob;
import org.cmdbuild.services.scheduler.trigger.JobTrigger;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzScheduler implements SchedulerService {

	private org.quartz.Scheduler scheduler;

	public QuartzScheduler() {
		try {
			scheduler = StdSchedulerFactory.getDefaultScheduler();
		} catch (final org.quartz.SchedulerException e) {
			throw SchedulerExceptionType.SCHEDULER_INTERNAL_ERROR.createException(e.getMessage());
		}
	}

	@Override
	public void addJob(final CMJob job, final JobTrigger trigger) {
		final Trigger qTrigger = new QuartzTriggerFactory().create(trigger);
		final JobDetail qJobDetail = QuartzJob.createJobDetail(job);
		try {
			scheduler.scheduleJob(qJobDetail, qTrigger);
		} catch (final org.quartz.SchedulerException e) {
			throw SchedulerExceptionType.SCHEDULER_INTERNAL_ERROR.createException(e.getMessage());
		}
	}

	@Override
	public void removeJob(final CMJob job) {
		try {
			scheduler.deleteJob(job.getName(), null);
		} catch (final org.quartz.SchedulerException e) {
			throw SchedulerExceptionType.SCHEDULER_INTERNAL_ERROR.createException(e.getMessage());
		}
	}

	@Override
	public void start() {
		try {
			scheduler.start();
		} catch (final org.quartz.SchedulerException e) {
			throw SchedulerExceptionType.SCHEDULER_INTERNAL_ERROR.createException(e.getMessage());
		}
	}

	@Override
	public void stop() {
		try {
			if (!scheduler.isShutdown()) {
				scheduler.shutdown(true);
			}
		} catch (final org.quartz.SchedulerException e) {
			throw SchedulerExceptionType.SCHEDULER_INTERNAL_ERROR.createException(e.getMessage());
		}
	}
}
