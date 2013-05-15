package org.cmdbuild.services.scheduler.job;

public interface Job {
	String getName();

	void execute();
}
