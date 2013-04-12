package org.cmdbuild.services.scheduler.trigger;

public abstract class JobTrigger {

	public abstract Object accept(TriggerVisitor factory);
}
