package org.cmdbuild.scheduler;

public class RecurringTrigger implements Trigger {

	private final String cronExpression;

	public RecurringTrigger(final String cronExpression) {
		this.cronExpression = cronExpression;
	}

	@Override
	public void accept(final TriggerVisitor visitor) {
		visitor.visit(this);
	}

	public String getCronExpression() {
		return cronExpression;
	}

}
