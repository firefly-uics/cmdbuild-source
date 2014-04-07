package org.cmdbuild.services.scheduler.job;

public interface CMJob {
	/**
	 * 
	 * @return the name used to identify the Job
	 */
	String getName();

	/**
	 * The method called by the scheduler
	 */
	void execute();
}
