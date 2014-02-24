package org.cmdbuild.logic.taskmanager;

import org.cmdbuild.model.scheduler.SchedulerJob;

public interface SchedulerFacade {

	void add(SchedulerJob schedulerJob);

	void modify(SchedulerJob schedulerJob);

	void delete(SchedulerJob schedulerJob);

}
