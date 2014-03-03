package org.cmdbuild.logic.scheduler;

import org.cmdbuild.data.store.scheduler.SchedulerJob;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.scheduler.SchedulerService;

public interface JobFactory {

	/**
	 * Returns the {@link SchedulerService}'s {@link Job} from the
	 * {@link SchedulerJob}.
	 * 
	 * @param schedulerJob
	 * 
	 * @return the {@link SchedulerService}'s {@link Job} from the
	 *         {@link SchedulerJob}, {@code null} if no {@link Job} can be
	 *         created.
	 */
	Job create(SchedulerJob schedulerJob);

}