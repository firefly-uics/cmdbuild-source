package org.cmdbuild.logic.taskmanager;

import static com.google.common.collect.FluentIterable.from;

import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.scheduler.EmailServiceSchedulerJob;
import org.cmdbuild.data.store.scheduler.SchedulerJob;
import org.cmdbuild.data.store.scheduler.SchedulerJobVisitor;
import org.cmdbuild.data.store.scheduler.WorkflowSchedulerJob;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.logic.scheduler.JobFactory;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.scheduler.RecurringTrigger;
import org.cmdbuild.scheduler.SchedulerService;
import org.cmdbuild.scheduler.Trigger;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;

public class DefaultScheduledTaskFacade implements ScheduledTaskFacade {

	private static final Logger logger = Logic.logger;
	private static final Marker MARKER = MarkerFactory.getMarker(DefaultScheduledTaskFacade.class.getName());

	private final ScheduledTaskFacadeConverterFactory scheduledTaskConverterFactory;
	private final Store<SchedulerJob> store;
	private final SchedulerService schedulerService;
	private final JobFactory jobFactory;

	public DefaultScheduledTaskFacade(final ScheduledTaskFacadeConverterFactory scheduledTaskConverterFactory,
			final Store<SchedulerJob> store, final SchedulerService schedulerService, final JobFactory jobFactory) {
		this.scheduledTaskConverterFactory = scheduledTaskConverterFactory;
		this.store = store;
		this.schedulerService = schedulerService;
		this.jobFactory = jobFactory;
	}

	@Override
	@Transactional
	public Long create(final ScheduledTask task) {
		logger.info(MARKER, "creating a new scheduled task '{}'", task);

		final SchedulerJob schedulerJob = scheduledTaskConverterFactory.of(task).toSchedulerJob();

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
	public Iterable<ScheduledTask> read() {
		logger.info(MARKER, "reading all scheduler's jobs");

		return from(store.list()) //
				.transform(toScheduledTask());
	}

	private Function<SchedulerJob, ScheduledTask> toScheduledTask() {
		return new Function<SchedulerJob, ScheduledTask>() {

			@Override
			public ScheduledTask apply(final SchedulerJob input) {
				return scheduledTaskConverterFactory.of(input).toScheduledTask();
			}

		};
	}

	@Override
	public ScheduledTask read(final ScheduledTask task) {
		logger.info(MARKER, "reading scheduled task detail");

		final SchedulerJob schedulerJob = scheduledTaskConverterFactory.of(task).toSchedulerJob();
		final SchedulerJob readed = store.read(schedulerJob);
		return scheduledTaskConverterFactory.of(readed).toScheduledTask();
	}

	@Override
	@Transactional
	public void update(final ScheduledTask task) {
		logger.info(MARKER, "updating an existing scheduled task '{}'", task);

		final SchedulerJob schedulerJob = scheduledTaskConverterFactory.of(task).toSchedulerJob();

		logger.debug(MARKER, "reading existing job");
		final SchedulerJob existing = store.read(schedulerJob);

		if (existing.isRunning()) {
			logger.debug(MARKER, "deleting scheduled job");
			final Job job = jobFactory.create(existing);
			schedulerService.remove(job);
		}

		logger.debug(MARKER, "updating existing job");
		existing.setDescription(schedulerJob.getDescription());
		existing.setRunning(schedulerJob.isRunning());
		existing.setCronExpression(schedulerJob.getCronExpression());
		schedulerJob.accept(new SchedulerJobVisitor() {

			@Override
			public void visit(final EmailServiceSchedulerJob schedulerJob) {
				throw new UnsupportedOperationException("TODO");
			}

			@Override
			public void visit(final WorkflowSchedulerJob schedulerJob) {
				final WorkflowSchedulerJob existingAsWorkflowSchedulerJob = WorkflowSchedulerJob.class.cast(existing);
				existingAsWorkflowSchedulerJob.setParameters(schedulerJob.getParameters());
			}

		});
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
	public void delete(final ScheduledTask task) {
		logger.info(MARKER, "updating an existing scheduled task '{}'", task);

		final SchedulerJob schedulerJob = scheduledTaskConverterFactory.of(task).toSchedulerJob();

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
