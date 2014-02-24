package org.cmdbuild.logic.scheduler;

import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.scheduler.SchedulerJob;

public interface SchedulerLogic extends Logic {

	Iterable<SchedulerJob> findAllScheduledJobs();

	Iterable<SchedulerJob> findJobsByDetail(String detail);

	void startScheduler();

	void stopScheduler();

	void addAllScheduledJobs();

}
