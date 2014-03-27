package org.cmdbuild.logic.taskmanager;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndObserverConverter.ObserverFactory;
import org.cmdbuild.services.event.Command;
import org.cmdbuild.services.event.DefaultObserver;
import org.cmdbuild.services.event.DefaultObserver.Builder;
import org.cmdbuild.services.event.FilteredObserver;
import org.cmdbuild.services.event.Observer;
import org.cmdbuild.services.event.SafeCommand;
import org.cmdbuild.services.event.ScriptCommand;

import com.google.common.base.Predicate;

public class DefaultObserverFactory implements ObserverFactory {

	private static class SynchronousEventTaskPredicate implements Predicate<CMCard> {

		public static SynchronousEventTaskPredicate of(final SynchronousEventTask task) {
			return new SynchronousEventTaskPredicate(task);
		}

		private final SynchronousEventTask task;

		private SynchronousEventTaskPredicate(final SynchronousEventTask task) {
			this.task = task;
		}

		@Override
		public boolean apply(final CMCard input) {
			return matchesClass(input);
		}

		private boolean matchesClass(final CMCard input) {
			return input.getType().getName().equals(task.getTargetClassname());
		}

	}

	@Override
	public Observer create(final SynchronousEventTask task) {
		final Builder builder = DefaultObserver.newInstance();
		final DefaultObserver.Phase phase = phaseOf(task);
		if (task.isScriptingEnabled()) {
			builder.add(scriptingOf(task), phase);
		}
		final DefaultObserver base = builder.build();
		return FilteredObserver.newInstance() //
				.withDelegate(base) //
				.withFilter(filterOf(task)) //
				.build();
	}

	private DefaultObserver.Phase phaseOf(final SynchronousEventTask task) {
		return new SynchronousEventTask.PhaseIdentifier() {

			private DefaultObserver.Phase converted;

			public org.cmdbuild.services.event.DefaultObserver.Phase toObserverPhase() {
				task.getPhase().identify(this);
				Validate.notNull(converted, "conversion error");
				return converted;
			}

			@Override
			public void afterCreate() {
				converted = DefaultObserver.Phase.AFTER_CREATE;
			}

			@Override
			public void beforeUpdate() {
				converted = DefaultObserver.Phase.BEFORE_UPDATE;
			}

			@Override
			public void afterUpdate() {
				converted = DefaultObserver.Phase.AFTER_UPDATE;
			}

			@Override
			public void beforeDelete() {
				converted = DefaultObserver.Phase.BEFORE_DELETE;
			}

		}.toObserverPhase();
	}

	private Command scriptingOf(final SynchronousEventTask task) {
		final Command command = ScriptCommand.newInstance() //
				.withEngine(task.getScriptingEngine()) //
				.withScript(task.getScriptingScript()) //
				.build();
		return task.isScriptingSafe() ? SafeCommand.of(command) : command;
	}

	private Predicate<CMCard> filterOf(final SynchronousEventTask task) {
		return SynchronousEventTaskPredicate.of(task);
	}

}
