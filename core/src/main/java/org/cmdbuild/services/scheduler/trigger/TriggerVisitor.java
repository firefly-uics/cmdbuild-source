package org.cmdbuild.services.scheduler.trigger;

public interface TriggerVisitor {

	public abstract Object visit(OneTimeTrigger trigger);

	public abstract Object visit(RecurringTrigger recurringTrigger);
}
