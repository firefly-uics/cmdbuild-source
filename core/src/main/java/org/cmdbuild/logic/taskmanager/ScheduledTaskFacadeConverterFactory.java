package org.cmdbuild.logic.taskmanager;

import org.cmdbuild.model.scheduler.SchedulerJob;

public interface ScheduledTaskFacadeConverterFactory {

	interface ScheduledTaskConverter {

		SchedulerJob toSchedulerJob();

	}

	interface SchedulerJobConverter {

		ScheduledTask toScheduledTask();

	}

	ScheduledTaskConverter of(ScheduledTask scheduledTask);

	SchedulerJobConverter of(SchedulerJob schedulerJob);

}
