package org.cmdbuild.logic.taskmanager;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.uniqueIndex;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

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
		public void visit(final ReadEmailTask task) {
			createdId = schedulerFacade.create(task);
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			createdId = schedulerFacade.create(task);
		}

	}

	private static class ReadAll implements TaskVistor {

		private static final Object NO_ARGUMENTS_REQUIRED = null;

		private static final Function<ScheduledTask, Long> BY_ID = new Function<ScheduledTask, Long>() {

			@Override
			public Long apply(final ScheduledTask input) {
				return input.getId();
			}

		};

		private final ScheduledTaskFacade schedulerFacade;

		private final Map<Long, Task> tasksById = Maps.newHashMap();

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
			return tasksById.values();
		}

		public Iterable<Task> execute(final Class<? extends Task> type) {
			return from(execute()) //
					.filter(instanceOf(type));
		}

		@Override
		public void visit(final ReadEmailTask task) {
			tasksById.putAll(uniqueIndex(schedulerFacade.read(), BY_ID));
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			tasksById.putAll(uniqueIndex(schedulerFacade.read(), BY_ID));
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
				public void visit(final ReadEmailTask task) {
					raw = schedulerFacade.read(task);
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
		public void visit(final ReadEmailTask task) {
			schedulerFacade.update(task);
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
		public void visit(final ReadEmailTask task) {
			schedulerFacade.delete(task);
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
	public void update(final Task task) {
		logger.info(MARKER, "updating an existing task '{}'", task);
		Validate.isTrue(task.getId() != null, "invalid id");
		update.execute(task);
	}

	@Override
	public void delete(final Task task) {
		logger.info(MARKER, "deleting an existing task '{}'", task);
		Validate.isTrue(task.getId() != null, "invalid id");
		delete.execute(task);
	}

}
