package org.cmdbuild.services.scheduler;

import org.cmdbuild.services.scheduler.job.Job;
import org.cmdbuild.services.scheduler.trigger.JobTrigger;

public interface SchedulerService {

	void addJob(Job job, JobTrigger trigger);

	void removeJob(Job job);

	void start();

	void stop();
}
