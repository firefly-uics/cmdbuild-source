package org.cmdbuild.logic.taskmanager;

import org.cmdbuild.model.scheduler.SchedulerJob;

public interface SchedulerFacade {

	/**
	 * Creates a new {@link SchedulerJob}.
	 */
	Long create(SchedulerJob schedulerJob);

	/**
	 * Reads all {@link SchedulerJob}s.
	 */
	Iterable<SchedulerJob> read();

	/**
	 * Reads {@link SchedulerJob}'s details.
	 */
	SchedulerJob read(SchedulerJob schedulerJob);

	/**
	 * Updates an existing {@link SchedulerJob}.
	 */
	void update(SchedulerJob schedulerJob);

	/**
	 * Deletes an existing {@link SchedulerJob}.
	 */
	void delete(SchedulerJob schedulerJob);

}
