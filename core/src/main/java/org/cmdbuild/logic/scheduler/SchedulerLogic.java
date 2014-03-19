package org.cmdbuild.logic.scheduler;

import org.cmdbuild.data.store.task.Task;
import org.cmdbuild.logic.Logic;

public interface SchedulerLogic extends Logic {

	Iterable<Task> findAllScheduledJobs();

	Iterable<Task> findWorkflowJobsByProcess(String classname);

	void startScheduler();

	void stopScheduler();

	void addAllScheduledJobs();

}
