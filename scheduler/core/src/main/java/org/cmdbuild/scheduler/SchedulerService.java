package org.cmdbuild.scheduler;

public interface SchedulerService {

	void addJob(SchedulerJob job, SchedulerTrigger trigger);

	void removeJob(SchedulerJob job);

	void start();

	void stop();

}
