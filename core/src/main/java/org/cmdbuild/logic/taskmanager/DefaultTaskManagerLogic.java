package org.cmdbuild.logic.taskmanager;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.FluentIterable.from;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

public class DefaultTaskManagerLogic implements TaskManagerLogic {

	private static final Marker MARKER = MarkerFactory.getMarker(DefaultTaskManagerLogic.class.getName());

	private static class Create implements TaskVistor {

		private final ScheduledTaskFacade schedulerFacade;

		private Long createdId;

		public Create(final ScheduledTaskFacade schedulerFacade) {
			this.schedulerFacade = schedulerFacade;
		}

		public Long execute(final Task task) {
			task.accept(this);
			return createdId;
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			createdId = schedulerFacade.create(task);
		}

	}

	private static class ReadAll implements TaskVistor {

		private static final Object NO_ARGUMENTS_REQUIRED = null;

		private final ScheduledTaskFacade schedulerFacade;

		private final List<Task> tasks = Lists.newArrayList();

		public ReadAll(final ScheduledTaskFacade schedulerFacade) {
			this.schedulerFacade = schedulerFacade;
		}

		public Iterable<Task> execute() {
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

		public Iterable<Task> execute(final Class<? extends Task> type) {
			return from(tasks) //
					.filter(instanceOf(type));
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			for (final Task element : schedulerFacade.read()) {
				tasks.add(element);
			}
		}

	}

	private static class Read {

		private final ScheduledTaskFacade schedulerFacade;

		public Read(final ScheduledTaskFacade schedulerFacade) {
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
					raw = schedulerFacade.read(task);
				}

			}.read();
			return detailed;
		}

	}

	private static class Update implements TaskVistor {

		private final ScheduledTaskFacade schedulerFacade;

		public Update(final ScheduledTaskFacade schedulerFacade) {
			this.schedulerFacade = schedulerFacade;
		}

		public void execute(final Task task) {
			task.accept(this);
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			schedulerFacade.update(task);
		}

	}

	private static class Delete implements TaskVistor {

		private final ScheduledTaskFacade schedulerFacade;

		public Delete(final ScheduledTaskFacade schedulerFacade) {
			this.schedulerFacade = schedulerFacade;
		}

		public void execute(final Task task) {
			task.accept(this);
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			schedulerFacade.delete(task);
		}

	}

	private final Create create;
	private final ReadAll readAll;
	private final Read read;
	private final Update update;
	private final Delete delete;

	public DefaultTaskManagerLogic(final ScheduledTaskFacade schedulerFacade) {
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
	public Iterable<Task> read(final Class<? extends Task> type) {
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
