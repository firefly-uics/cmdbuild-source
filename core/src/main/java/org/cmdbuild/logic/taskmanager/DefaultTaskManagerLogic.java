package org.cmdbuild.logic.taskmanager;

import static com.google.common.collect.FluentIterable.from;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.cmdbuild.model.scheduler.SchedulerJob;
import org.cmdbuild.model.scheduler.WorkflowSchedulerJob;
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
			final SchedulerJob schedulerJob = new WorkflowSchedulerJob(input.getId());
			schedulerJob.setDescription(input.getDescription());
			schedulerJob.setRunning(input.isActive());
			schedulerJob.setCronExpression(input.getCronExpression());
			schedulerJob.setDetail(input.getProcessClass());
			schedulerJob.setLegacyParameters(input.getParameters());
			return schedulerJob;
		};

	};

	private static class Create implements TaskVistor {

		private final SchedulerFacade schedulerFacade;

		private Long createdId;

		public Create(final SchedulerFacade schedulerFacade) {
			this.schedulerFacade = schedulerFacade;
		}

		public Long execute(final Task task) {
			task.accept(this);
			return createdId;
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			final SchedulerJob schedulerJob = START_WORKFLOW_TASK_TO_SCHEDULER_JOB.apply(task);
			createdId = schedulerFacade.create(schedulerJob);
		}

	}

	private static class ReadAll implements TaskVistor {

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

		public ReadAll(final SchedulerFacade schedulerFacade) {
			this.schedulerFacade = schedulerFacade;
		}

		public Iterable<? extends Task> execute() {
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

		public Iterable<? extends Task> execute(final Class<? extends Task> type) {
			return from(tasks) //
					.filter(type);
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			final Iterable<? extends SchedulerJob> schedulerJobs = schedulerFacade.read();
			Iterables.addAll(tasks, from(schedulerJobs) //
					.transform(SCHEDULER_JOB_TO_TASK));
		}

	}

	private static class Read {

		private static final Function<SchedulerJob, StartWorkflowTask> SCHEDULER_JOB_TO_START_WORKFLOW_TASK = new Function<SchedulerJob, StartWorkflowTask>() {

			@Override
			public StartWorkflowTask apply(final SchedulerJob input) {
				return StartWorkflowTask.newInstance() //
						.withId(input.getId()) //
						.withDescription(input.getDescription()) //
						.withActiveStatus(input.isRunning()) //
						.withCronExpression(input.getCronExpression()) //
						.withProcessClass(input.getDetail()) //
						.withParameters(input.getLegacyParameters()) //
						.build();
			}

		};

		private final SchedulerFacade schedulerFacade;

		public Read(final SchedulerFacade schedulerFacade) {
			this.schedulerFacade = schedulerFacade;
		}

		public <T extends Task> T execute(final T task, final Class<T> type) {
			final T detailed = new TaskVistor() {

				private Task raw;

				public T read() {
					task.accept(this);
					return type.cast(raw);
				}

				@Override
				public void visit(final StartWorkflowTask task) {
					final SchedulerJob schedulerJob = START_WORKFLOW_TASK_TO_SCHEDULER_JOB.apply(task);
					final SchedulerJob readed = schedulerFacade.read(schedulerJob);
					raw = SCHEDULER_JOB_TO_START_WORKFLOW_TASK.apply(readed);
				}

			}.read();
			return detailed;
		}

	}

	private static class Update implements TaskVistor {

		private final SchedulerFacade schedulerFacade;

		public Update(final SchedulerFacade schedulerFacade) {
			this.schedulerFacade = schedulerFacade;
		}

		public void execute(final Task task) {
			task.accept(this);
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			final SchedulerJob schedulerJob = START_WORKFLOW_TASK_TO_SCHEDULER_JOB.apply(task);
			schedulerFacade.update(schedulerJob);
		}

	}

	private static class Delete implements TaskVistor {

		private final SchedulerFacade schedulerFacade;

		public Delete(final SchedulerFacade schedulerFacade) {
			this.schedulerFacade = schedulerFacade;
		}

		public void execute(final Task task) {
			task.accept(this);
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			final SchedulerJob schedulerJob = START_WORKFLOW_TASK_TO_SCHEDULER_JOB.apply(task);
			schedulerFacade.delete(schedulerJob);
		}

	}

	private final Create create;
	private final ReadAll readAll;
	private final Read read;
	private final Update update;
	private final Delete delete;

	public DefaultTaskManagerLogic(final SchedulerFacade schedulerFacade) {
		create = new Create(schedulerFacade);
		readAll = new ReadAll(schedulerFacade);
		read = new Read(schedulerFacade);
		update = new Update(schedulerFacade);
		delete = new Delete(schedulerFacade);
	}

	@Override
	@Transactional
	public Long create(final Task task) {
		logger.info(MARKER, "creating a new task '{}'", task);
		return create.execute(task);
	}

	@Override
	public Iterable<? extends Task> read() {
		logger.info(MARKER, "reading all existing tasks");
		return readAll.execute();
	}

	@Override
	public Iterable<? extends Task> read(final Class<? extends Task> type) {
		logger.info(MARKER, "reading all existing tasks for type '{}'", type);
		return readAll.execute(type);
	}

	@Override
	public <T extends Task> T read(final T task, final Class<T> type) {
		logger.info(MARKER, "reading task's details of '{}'", task);
		return read.execute(task, type);
	}

	@Override
	@Transactional
	public void update(final Task task) {
		logger.info(MARKER, "updating an existing task '{}'", task);
		Validate.isTrue(task.getId() != null, "invalid id");
		update.execute(task);
	}

	@Override
	@Transactional
	public void delete(final Task task) {
		logger.info(MARKER, "deleting an existing task '{}'", task);
		Validate.isTrue(task.getId() != null, "invalid id");
		delete.execute(task);
	}

}
