package org.cmdbuild.logic.taskmanager;

import org.apache.commons.lang.Validate;
import org.cmdbuild.model.scheduler.SchedulerJob;
import org.cmdbuild.model.scheduler.SchedulerJob.Type;
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

		private final SchedulerFacade schedulerFacade;

		public TaskAdder(final SchedulerFacade schedulerFacade) {
			this.schedulerFacade = schedulerFacade;
		}

		public void add(final Task task) {
			task.accept(this);
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			logger.debug(MARKER, "adding task '{}' as a scheduled job", task);

			final SchedulerJob schedulerJob = START_WORKFLOW_TASK_TO_SCHEDULER_JOB.apply(task);

			logger.debug(MARKER, "storing job");
			schedulerFacade.add(schedulerJob);
		}

	}

	private static class TaskDeleter implements TaskVistor {

		private final SchedulerFacade schedulerFacade;

		public TaskDeleter(final SchedulerFacade schedulerFacade) {
			this.schedulerFacade = schedulerFacade;
		}

		public void delete(final Task task) {
			task.accept(this);
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			logger.debug(MARKER, "deleting task '{}' as a scheduled job", task);

			final SchedulerJob schedulerJob = START_WORKFLOW_TASK_TO_SCHEDULER_JOB.apply(task);

			logger.debug(MARKER, "storing job");
			schedulerFacade.delete(schedulerJob);
		}

	}

	private final TaskAdder adder;
	private final TaskDeleter deleter;

	public DefaultTaskManagerLogic(final SchedulerFacade schedulerFacade) {
		adder = new TaskAdder(schedulerFacade);
		deleter = new TaskDeleter(schedulerFacade);
	}

	@Override
	@Transactional
	public void add(final Task task) {
		logger.info(MARKER, "adding task '{}'", task);
		adder.add(task);
	}

	@Override
	@Transactional
	public void delete(final Task task) {
		logger.info(MARKER, "deleting task '{}'", task);

		Validate.isTrue(task.getId() != null, "invalid id");
		deleter.delete(task);
	}

}
