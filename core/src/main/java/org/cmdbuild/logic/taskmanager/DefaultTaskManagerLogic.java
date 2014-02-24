package org.cmdbuild.logic.taskmanager;

import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.logic.scheduler.JobFactory;
import org.cmdbuild.model.scheduler.SchedulerJob;
import org.cmdbuild.model.scheduler.SchedulerJob.Type;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.scheduler.RecurringTrigger;
import org.cmdbuild.scheduler.SchedulerService;
import org.cmdbuild.scheduler.Trigger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;

public class DefaultTaskManagerLogic implements TaskManagerLogic {

	private static final Marker MARKER = MarkerFactory.getMarker(DefaultTaskManagerLogic.class.getName());

	private static final Function<StartWorkflowTask, SchedulerJob> START_WORKFLOW_TASK_TO_SCHEDULER_JOB = new Function<StartWorkflowTask, SchedulerJob>() {

		@Override
		public SchedulerJob apply(final StartWorkflowTask input) {
			final SchedulerJob schedulerJob = new SchedulerJob(input.getId());
			schedulerJob.setCode(input.getName());
			schedulerJob.setDescription(input.getDescription());
			schedulerJob.setRunning(input.isActive());
			schedulerJob.setCronExpression(input.getCronExpression());
			schedulerJob.setType(Type.workflow);
			schedulerJob.setDetail(input.getProcessClass());
			schedulerJob.setLegacyParameters(input.getParameters());
			return schedulerJob;
		};

	};

	private static class TaskAdder implements TaskVistor {

		private final Store<SchedulerJob> store;
		private final SchedulerService schedulerService;
		private final JobFactory jobFactory;

		public TaskAdder(final Store<SchedulerJob> store, final SchedulerService schedulerService,
				final JobFactory jobFactory) {
			this.store = store;
			this.schedulerService = schedulerService;
			this.jobFactory = jobFactory;
		}

		public void add(final Task task) {
			task.accept(this);
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			logger.debug(MARKER, "adding task '{}' as a scheduled job", task);

			final SchedulerJob schedulerJob = START_WORKFLOW_TASK_TO_SCHEDULER_JOB.apply(task);

			logger.debug(MARKER, "storing job");
			final Storable created = store.create(schedulerJob);
			final SchedulerJob readed = store.read(created);

			logger.debug(MARKER, "scheduling job", readed);
			final Job serviceJob = jobFactory.create(readed);

			logger.debug("creating trigger from cron expression");
			final Trigger trigger = RecurringTrigger.at(readed.getCronExpression());
			schedulerService.add(serviceJob, trigger);
		}

	}

	private final TaskAdder adder;

	public DefaultTaskManagerLogic(final Store<SchedulerJob> store, final SchedulerService schedulerService,
			final JobFactory jobFactory) {
		adder = new TaskAdder(store, schedulerService, jobFactory);
	}

	@Override
	@Transactional
	public void add(final Task task) {
		logger.info(MARKER, "adding task '{}'", task);
		adder.add(task);
	}

}
