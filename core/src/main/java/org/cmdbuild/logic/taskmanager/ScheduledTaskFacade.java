package org.cmdbuild.logic.taskmanager;

public interface ScheduledTaskFacade {

	/**
	 * Creates a new {@link ScheduledTask}.
	 */
	Long create(ScheduledTask task);

	/**
	 * Reads all {@link ScheduledTask}s.
	 */
	Iterable<ScheduledTask> read();

	/**
	 * Reads {@link ScheduledTask}'s details.
	 */
	ScheduledTask read(ScheduledTask task);

	/**
	 * Updates an existing {@link ScheduledTask}.
	 */
	void update(ScheduledTask task);

	/**
	 * Deletes an existing {@link ScheduledTask}.
	 */
	void delete(ScheduledTask task);

}
