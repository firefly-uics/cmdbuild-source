package org.cmdbuild.logic.taskmanager;

import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.logic.scheduler.JobFactory;
import org.cmdbuild.model.scheduler.SchedulerJob;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.scheduler.RecurringTrigger;
import org.cmdbuild.scheduler.SchedulerService;
import org.cmdbuild.scheduler.Trigger;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.transaction.annotation.Transactional;

public class DefaultSchedulerFacade implements SchedulerFacade {

	private static final Logger logger = Logic.logger;
	private static final Marker MARKER = MarkerFactory.getMarker(DefaultSchedulerFacade.class.getName());

	private final Store<SchedulerJob> store;
	private final SchedulerService schedulerService;
	private final JobFactory jobFactory;

	public DefaultSchedulerFacade(final Store<SchedulerJob> store, final SchedulerService schedulerService,
			final JobFactory jobFactory) {
		this.store = store;
		this.schedulerService = schedulerService;
		this.jobFactory = jobFactory;
	}

	@Override
	@Transactional
	public Long create(final SchedulerJob schedulerJob) {
		logger.info(MARKER, "creating a new scheduler's job '{}'", schedulerJob);

		logger.debug(MARKER, "storing job");
		final Storable created = store.create(schedulerJob);
		final SchedulerJob readed = store.read(created);

		if (schedulerJob.isRunning()) {
			logger.debug(MARKER, "scheduling job", readed);
			final Job serviceJob = jobFactory.create(readed);
			final Trigger trigger = RecurringTrigger.at(readed.getCronExpression());
			schedulerService.add(serviceJob, trigger);
		}

		return readed.getId();
	}

	@Override
	public Iterable<SchedulerJob> read() {
		logger.info(MARKER, "reading all scheduler's jobs");

		return store.list();
	}

	@Override
	@Transactional
	public void update(final SchedulerJob schedulerJob) {
		logger.info(MARKER, "updating an existing scheduler's job '{}'", schedulerJob);

		logger.debug(MARKER, "reading existing job");
		final SchedulerJob existing = store.read(schedulerJob);

		if (existing.isRunning()) {
			logger.debug(MARKER, "deleting scheduled job");
			final Job job = jobFactory.create(schedulerJob);
			schedulerService.remove(job);
		}

		logger.debug(MARKER, "updating existing job");
		existing.setDescription(schedulerJob.getDescription());
		existing.setRunning(schedulerJob.isRunning());
		existing.setCronExpression(schedulerJob.getCronExpression());
		existing.setLegacyParameters(schedulerJob.getLegacyParameters());
		store.update(existing);

		if (schedulerJob.isRunning()) {
			logger.debug(MARKER, "scheduling job", existing);
			final Job serviceJob = jobFactory.create(existing);
			final Trigger trigger = RecurringTrigger.at(existing.getCronExpression());
			schedulerService.add(serviceJob, trigger);
		}
	}

	@Override
	@Transactional
	public void delete(final SchedulerJob schedulerJob) {
		logger.info(MARKER, "updating an existing scheduler's job '{}'", schedulerJob);

		logger.debug(MARKER, "deleting stored job");
		final SchedulerJob existing = store.read(schedulerJob);
		store.delete(existing);

		if (existing.isRunning()) {
			logger.debug(MARKER, "deleting scheduled job");
			final Job job = jobFactory.create(existing);
			schedulerService.remove(job);
		}
	}

}
