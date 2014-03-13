package org.cmdbuild.logic.taskmanager;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.uniqueIndex;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class DefaultTaskManagerLogic implements TaskManagerLogic {

	private static final Marker MARKER = MarkerFactory.getMarker(DefaultTaskManagerLogic.class.getName());

	private static interface Action<T> {

		T execute();

	}

	private static class Create implements Action<Long>, TaskVistor {

		private final ScheduledTaskFacade schedulerFacade;
		private final Task task;

		private Long createdId;

		public Create(final ScheduledTaskFacade schedulerFacade, final Task task) {
			this.schedulerFacade = schedulerFacade;
			this.task = task;
		}

		@Override
		public Long execute() {
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

	private static class ReadAll implements Action<Iterable<Task>>, TaskVistor {

		private static final Object NO_ARGUMENTS_REQUIRED = null;

		private static final Function<ScheduledTask, Long> BY_ID = new Function<ScheduledTask, Long>() {

			@Override
			public Long apply(final ScheduledTask input) {
				return input.getId();
			}

		};

		private static Class<Object> ALL_TYPES = Object.class;

		private final ScheduledTaskFacade schedulerFacade;
		private final Class<?> type;

		private final Map<Long, Task> tasksById = Maps.newHashMap();

		public ReadAll(final ScheduledTaskFacade schedulerFacade) {
			this(schedulerFacade, null);
		}

		public ReadAll(final ScheduledTaskFacade schedulerFacade, final Class<? extends Task> type) {
			this.schedulerFacade = schedulerFacade;
			this.type = (type == null) ? ALL_TYPES : type;
		}

		@Override
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
			return from(tasksById.values()) //
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

	private static class Read<T extends Task> implements Action<T>, TaskVistor {

		private final ScheduledTaskFacade schedulerFacade;
		private final T task;
		private final Class<T> type;

		private Task raw;

		public Read(final ScheduledTaskFacade schedulerFacade, final T task, final Class<T> type) {
			this.schedulerFacade = schedulerFacade;
			this.task = task;
			this.type = type;
		}

		@Override
		public T execute() {
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

	}

	private static class Update implements Action<Void>, TaskVistor {

		private final ScheduledTaskFacade schedulerFacade;
		private final Task task;

		public Update(final ScheduledTaskFacade schedulerFacade, final Task task) {
			this.schedulerFacade = schedulerFacade;
			this.task = task;
		}

		@Override
		public Void execute() {
			task.accept(this);
			return null;
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

	private static class Delete implements Action<Void>, TaskVistor {

		private final ScheduledTaskFacade schedulerFacade;
		private final Task task;

		public Delete(final ScheduledTaskFacade schedulerFacade, final Task task) {
			this.schedulerFacade = schedulerFacade;
			this.task = task;
		}

		@Override
		public Void execute() {
			task.accept(this);
			return null;
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

	private final ScheduledTaskFacade schedulerFacade;

	public DefaultTaskManagerLogic(final ScheduledTaskFacade schedulerFacade) {
		this.schedulerFacade = schedulerFacade;
	}

	@Override
	public Long create(final Task task) {
		logger.info(MARKER, "creating a new task '{}'", task);
		return execute(new Create(schedulerFacade, task));
	}

	@Override
	public Iterable<Task> read() {
		logger.info(MARKER, "reading all existing tasks");
		return execute(new ReadAll(schedulerFacade));
	}

	@Override
	public Iterable<Task> read(final Class<? extends Task> type) {
		logger.info(MARKER, "reading all existing tasks for type '{}'", type);
		return execute(new ReadAll(schedulerFacade, type));
	}

	@Override
	public <T extends Task> T read(final T task, final Class<T> type) {
		logger.info(MARKER, "reading task's details of '{}'", task);
		return execute(new Read<T>(schedulerFacade, task, type));
	}

	@Override
	public void update(final Task task) {
		logger.info(MARKER, "updating an existing task '{}'", task);
		Validate.isTrue(task.getId() != null, "invalid id");
		execute(new Update(schedulerFacade, task));
	}

	@Override
	public void delete(final Task task) {
		logger.info(MARKER, "deleting an existing task '{}'", task);
		Validate.isTrue(task.getId() != null, "invalid id");
		execute(new Delete(schedulerFacade, task));
	}

	private <T> T execute(final Action<T> action) {
		return action.execute();
	}

}
