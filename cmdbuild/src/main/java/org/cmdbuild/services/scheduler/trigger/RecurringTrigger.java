package org.cmdbuild.services.scheduler.trigger;


public class RecurringTrigger extends JobTrigger {

	String cronExpression;

	public RecurringTrigger(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public Object accept(TriggerVisitor factory) {
		return factory.visit(this);
	}
}
