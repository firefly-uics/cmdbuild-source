package org.cmdbuild.logic.scheduler;

import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.scheduler.SchedulerJob;

public interface SchedulerLogic extends Logic {

	Iterable<SchedulerJob> findAllScheduledJobs();

	Iterable<SchedulerJob> findJobsByDetail(String detail);

	SchedulerJob createAndStart(SchedulerJob job);

	/**
	 * Updates description, cron expression and (legacy) parameters for the
	 * specified job.
	 * 
	 * @param job
	 * 
	 * @return the updated job.
	 */
	SchedulerJob update(SchedulerJob job);

	void delete(Long jobId);

	void startScheduler();

	void stopScheduler();

	void addAllScheduledJobs();

}
