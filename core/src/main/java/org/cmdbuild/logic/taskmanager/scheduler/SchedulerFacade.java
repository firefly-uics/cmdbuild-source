package org.cmdbuild.logic.taskmanager.scheduler;

import org.cmdbuild.logic.taskmanager.ScheduledTask;

public interface SchedulerFacade {

	interface Callback {

		void start();

		void stop();

	}

	/**
	 * Creates a new {@link ScheduledTask}.
	 */
	void create(ScheduledTask task, Callback callback);

	/**
	 * Deletes an existing {@link ScheduledTask}.
	 */
	void delete(ScheduledTask task);

}
