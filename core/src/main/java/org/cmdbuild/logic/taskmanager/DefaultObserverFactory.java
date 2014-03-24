package org.cmdbuild.logic.taskmanager;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndObserverConverter.ObserverFactory;
import org.cmdbuild.services.event.DefaultObserver;
import org.cmdbuild.services.event.DefaultObserver.Builder;
import org.cmdbuild.services.event.Observer;
import org.cmdbuild.services.event.ScriptCommand;

public class DefaultObserverFactory implements ObserverFactory {

	@Override
	public Observer create(final SynchronousEventTask task) {
		final Builder builder = DefaultObserver.newInstance();
		final DefaultObserver.Phase phase = toObserverPhase(task.getPhase());
		if (task.isScriptingEnabled()) {
			builder.add(ScriptCommand.newInstance() //
					.withEngine(task.getScriptingEngine()) //
					.withScript(task.getScriptingScript()) //
					.build(), phase);
		}
		return builder.build();
	}

	private DefaultObserver.Phase toObserverPhase(final SynchronousEventTask.Phase phase) {
		return new SynchronousEventTask.PhaseIdentifier() {

			private DefaultObserver.Phase converted;

			public org.cmdbuild.services.event.DefaultObserver.Phase toObserverPhase() {
				phase.identify(this);
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
}
