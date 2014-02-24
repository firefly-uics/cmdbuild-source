package org.cmdbuild.logic.scheduler;

import java.util.List;

import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.model.scheduler.SchedulerJob;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.scheduler.RecurringTrigger;
import org.cmdbuild.scheduler.SchedulerService;
import org.cmdbuild.scheduler.Trigger;
import org.springframework.transaction.annotation.Transactional;

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
	public Iterable<SchedulerJob> findJobsByDetail(final String detail) {
		logger.info("finding all jobs with detail '{}'", detail);
		final List<SchedulerJob> filtered = Lists.newArrayList();
		for (final SchedulerJob element : store.list()) {
			if (detail.equals(element.getDetail())) {
				filtered.add(element);
			}
		}
		return filtered;
	}

	@Override
	@Transactional
	public SchedulerJob update(final SchedulerJob job) {
		logger.info("updating job '{}'", job.getIdentifier());
		schedulerService.remove(jobFactory.create(job));
		final SchedulerJob schedulerJob = store.read(job);
		schedulerJob.setDescription(job.getDescription());
		schedulerJob.setLegacyParameters(job.getLegacyParameters());
		schedulerJob.setCronExpression(job.getCronExpression());
		store.update(schedulerJob);
		schedule(job);
		return schedulerJob;
	}

	@Override
	@Transactional
	public void delete(final Long jobId) {
		logger.info("deleting job '{}'", jobId);
		final SchedulerJob schedulerJob = store.read(storableFrom(jobId));
		store.delete(schedulerJob);
		schedulerService.remove(jobFactory.create(schedulerJob));
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

	private Storable storableFrom(final Long id) {
		return new Storable() {

			@Override
			public String getIdentifier() {
				return id.toString();
			}

		};
	}

}
