package org.cmdbuild.logic.taskmanager;

import org.cmdbuild.model.scheduler.SchedulerJob;

public interface SchedulerFacade {

	void add(SchedulerJob schedulerJob);

}
