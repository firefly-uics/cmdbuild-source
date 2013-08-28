package org.cmdbuild.scheduler.quartz;

import org.cmdbuild.scheduler.SchedulerExeptionFactory;
import org.cmdbuild.scheduler.SchedulerJob;
import org.cmdbuild.scheduler.SchedulerService;
import org.cmdbuild.scheduler.SchedulerTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzSchedulerService implements SchedulerService {

	private final SchedulerExeptionFactory exeptionFactory;

	private Scheduler scheduler;

	public QuartzSchedulerService(final SchedulerExeptionFactory exeptionFactory) {
		this.exeptionFactory = exeptionFactory;
		try {
			scheduler = StdSchedulerFactory.getDefaultScheduler();
		} catch (final SchedulerException e) {
			throw exeptionFactory.internal(e);
		}
	}

	@Override
	public void addJob(final SchedulerJob job, final SchedulerTrigger trigger) {
		final Trigger quartzTrigger = new QuartzTriggerFactory(exeptionFactory).create(trigger);
		final JobDetail jobDetail = QuartzJob.createJobDetail(job);
		try {
			scheduler.scheduleJob(jobDetail, quartzTrigger);
		} catch (final SchedulerException e) {
			throw exeptionFactory.internal(e);
		}
	}

	@Override
	public void removeJob(final SchedulerJob job) {
		try {
			final JobKey jobKey = QuartzJob.createJobKey(job);
			scheduler.deleteJob(jobKey);
		} catch (final SchedulerException e) {
			throw exeptionFactory.internal(e);
		}
	}

	@Override
	public void start() {
		try {
			scheduler.start();
		} catch (final SchedulerException e) {
			throw exeptionFactory.internal(e);
		}
	}

	@Override
	public void stop() {
		try {
			if (!scheduler.isShutdown()) {
				scheduler.shutdown(true);
			}
		} catch (final SchedulerException e) {
			throw exeptionFactory.internal(e);
		}
	}

}
