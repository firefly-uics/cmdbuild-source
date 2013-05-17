package org.cmdbuild.services.scheduler.trigger;

public class RecurringTrigger extends JobTrigger {

	String cronExpression;

	public RecurringTrigger(final String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	@Override
	public Object accept(final TriggerVisitor factory) {
		return factory.visit(this);
	}
}
