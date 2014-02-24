package org.cmdbuild.logic.taskmanager;

import static com.google.common.collect.FluentIterable.from;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.cmdbuild.model.scheduler.SchedulerJob;
import org.cmdbuild.model.scheduler.SchedulerJob.Type;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

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

		private Long createdId;

		public TaskAdder(final SchedulerFacade schedulerFacade) {
			this.schedulerFacade = schedulerFacade;
		}

		public Long add(final Task task) {
			task.accept(this);
			return createdId;
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			logger.debug(MARKER, "adding task '{}' as a scheduled job", task);

			final SchedulerJob schedulerJob = START_WORKFLOW_TASK_TO_SCHEDULER_JOB.apply(task);

			logger.debug(MARKER, "storing job");
			createdId = schedulerFacade.create(schedulerJob);
		}

	}

	private static class TaskReader implements TaskVistor {

		private static final Object NO_ARGUMENTS_REQUIRED = null;

		private static final Function<SchedulerJob, Task> SCHEDULER_JOB_TO_TASK = new Function<SchedulerJob, Task>() {

			@Override
			public Task apply(final SchedulerJob input) {
				// TODO make it extensible
				return StartWorkflowTask.newInstance() //
						.withId(input.getId()) //
						.withDescription(input.getDescription()) //
						.withActiveStatus(input.isRunning()) //
						.build();
			}

		};

		private final SchedulerFacade schedulerFacade;

		private final List<Task> tasks = Lists.newArrayList();

		public TaskReader(final SchedulerFacade schedulerFacade) {
			this.schedulerFacade = schedulerFacade;
		}

		public Iterable<? extends Task> read() {
			logger.debug(MARKER, "reading tasks");
			assert this instanceof TaskVistor;
			for (final Method method : TaskVistor.class.getMethods()) {
				try {
					method.invoke(this, NO_ARGUMENTS_REQUIRED);
				} catch (final IllegalArgumentException e) {
					logger.warn(MARKER, "error invoking method", e);
				} catch (final IllegalAccessException e) {
					logger.warn(MARKER, "error invoking method", e);
				} catch (final InvocationTargetException e) {
					logger.warn(MARKER, "error invoking method", e);
				}
			}
			return tasks;
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			final Iterable<? extends SchedulerJob> schedulerJobs = schedulerFacade.read();
			Iterables.addAll(tasks, from(schedulerJobs) //
					.transform(SCHEDULER_JOB_TO_TASK));
		}

	}

	private static class TaskModifier implements TaskVistor {

		private final SchedulerFacade schedulerFacade;

		public TaskModifier(final SchedulerFacade schedulerFacade) {
			this.schedulerFacade = schedulerFacade;
		}

		public void modify(final Task task) {
			task.accept(this);
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			logger.debug(MARKER, "modifying task '{}' as a scheduled job", task);

			final SchedulerJob schedulerJob = START_WORKFLOW_TASK_TO_SCHEDULER_JOB.apply(task);

			logger.debug(MARKER, "modifying job");
			schedulerFacade.update(schedulerJob);
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

			logger.debug(MARKER, "deleting job");
			schedulerFacade.delete(schedulerJob);
		}

	}

	private final TaskAdder adder;
	private final TaskReader reader;
	private final TaskModifier modifier;
	private final TaskDeleter deleter;

	public DefaultTaskManagerLogic(final SchedulerFacade schedulerFacade) {
		adder = new TaskAdder(schedulerFacade);
		reader = new TaskReader(schedulerFacade);
		modifier = new TaskModifier(schedulerFacade);
		deleter = new TaskDeleter(schedulerFacade);
	}

	@Override
	@Transactional
	public Long create(final Task task) {
		logger.info(MARKER, "creating a new task '{}'", task);
		return adder.add(task);
	}

	@Override
	public Iterable<? extends Task> read() {
		logger.info(MARKER, "reading all existing tasks");
		return reader.read();
	}

	@Override
	@Transactional
	public void update(final Task task) {
		logger.info(MARKER, "updating an existing task '{}'", task);
		Validate.isTrue(task.getId() != null, "invalid id");
		modifier.modify(task);
	}

	@Override
	@Transactional
	public void delete(final Task task) {
		logger.info(MARKER, "deleting an existing task '{}'", task);
		Validate.isTrue(task.getId() != null, "invalid id");
		deleter.delete(task);
	}

}
