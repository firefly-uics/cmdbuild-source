package org.cmdbuild.logic.scheduler;

import org.cmdbuild.data.store.scheduler.SchedulerJob;
import org.cmdbuild.logic.Logic;

public interface SchedulerLogic extends Logic {

	Iterable<SchedulerJob> findAllScheduledJobs();

	Iterable<SchedulerJob> findWorkflowJobsByProcess(String classname);

	void startScheduler();

	void stopScheduler();

	void addAllScheduledJobs();

}
