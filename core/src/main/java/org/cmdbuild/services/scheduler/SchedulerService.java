package org.cmdbuild.services.scheduler;

import org.cmdbuild.services.scheduler.job.CMJob;
import org.cmdbuild.services.scheduler.trigger.JobTrigger;

public interface SchedulerService {

	void addJob(CMJob job, JobTrigger trigger);

	void removeJob(CMJob job);

	void start();

	void stop();
}
