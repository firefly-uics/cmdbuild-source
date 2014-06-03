package org.cmdbuild.logic.taskmanager;

public interface SchedulerFacade {

	interface Callback {

		void start();

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
