package org.cmdbuild.logic.scheduler;

import java.util.List;

import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.scheduler.EmailServiceSchedulerJob;
import org.cmdbuild.data.store.scheduler.SchedulerJob;
import org.cmdbuild.data.store.scheduler.SchedulerJobVisitor;
import org.cmdbuild.data.store.scheduler.WorkflowSchedulerJob;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.scheduler.RecurringTrigger;
import org.cmdbuild.scheduler.SchedulerService;
import org.cmdbuild.scheduler.Trigger;

import com.google.common.collect.Lists;

public class DefaultSchedulerLogic implements SchedulerLogic {

	private final SchedulerService schedulerService;
	private final Store<SchedulerJob> store;
	private final JobFactory jobFactory;

	public DefaultSchedulerLogic( //
			final Store<SchedulerJob> store, //
			final SchedulerService schedulerService, //
			final JobFactory jobFactory) {
		this.store = store;
		this.jobFactory = jobFactory;
		this.schedulerService = schedulerService;
	}

	@Override
	public Iterable<SchedulerJob> findAllScheduledJobs() {
		logger.info("finding all scheduled jobs");
		return store.list();
	}

	@Override
	public Iterable<SchedulerJob> findWorkflowJobsByProcess(final String classname) {
		logger.info("finding all jobs with detail '{}'", classname);
		final List<SchedulerJob> filtered = Lists.newArrayList();
		for (final SchedulerJob element : store.list()) {
			element.accept(new SchedulerJobVisitor() {

				@Override
				public void visit(final EmailServiceSchedulerJob schedulerJob) {
					throw new UnsupportedOperationException("TODO");
				}

				@Override
				public void visit(final WorkflowSchedulerJob schedulerJob) {
					if (classname.equals(schedulerJob.getProcessClass())) {
						filtered.add(element);
					}
				}

			});
		}
		return filtered;
	}

	@Override
	public void startScheduler() {
		logger.info("starting scheduler service");
		schedulerService.start();
	}

	@Override
	public void stopScheduler() {
		logger.info("stopping scheduler service");
		schedulerService.stop();
	}

	@Override
	public void addAllScheduledJobs() {
		logger.info("adding all scheduled jobs");
		try {
			for (final SchedulerJob schedulerJob : findAllScheduledJobs()) {
				if (!schedulerJob.isRunning()) {
					continue;
				}
				try {
					schedule(schedulerJob);
				} catch (final Exception e) {
					logger.error("error scheduling job", e);
				}
			}
		} catch (final CMDBException e) {
			logger.warn("could not load scheduled jobs: first start or patch not yet applied?");
		}
	}

	private void schedule(final SchedulerJob schedulerJob) {
		logger.debug("scheduling job: {}", schedulerJob);
		final Job serviceJob = jobFactory.create(schedulerJob);
		if (serviceJob == null) {
			logger.warn("error creating service job");
		} else {
			logger.debug("creating recurring trigger from cron expression '{}'", schedulerJob.getCronExpression());
			final Trigger trigger = RecurringTrigger.at(schedulerJob.getCronExpression());
			schedulerService.add(serviceJob, trigger);
		}
	}

}
