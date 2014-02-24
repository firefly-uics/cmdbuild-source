package org.cmdbuild.logic.taskmanager;

import org.cmdbuild.model.scheduler.SchedulerJob;

public interface SchedulerFacade {

	Long add(SchedulerJob schedulerJob);

	Iterable<SchedulerJob> read();

	void modify(SchedulerJob schedulerJob);

	void delete(SchedulerJob schedulerJob);

}
