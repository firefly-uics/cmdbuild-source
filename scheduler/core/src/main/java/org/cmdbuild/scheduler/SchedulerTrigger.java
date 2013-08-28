package org.cmdbuild.scheduler;

public interface SchedulerTrigger {

	void accept(TriggerVisitor visitor);

}
