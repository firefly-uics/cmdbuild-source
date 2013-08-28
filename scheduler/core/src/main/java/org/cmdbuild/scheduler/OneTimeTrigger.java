package org.cmdbuild.scheduler;

import java.util.Date;

public class OneTimeTrigger implements SchedulerTrigger {

	private final Date date;

	public OneTimeTrigger(final Date date) {
		this.date = date;
	}

	@Override
	public void accept(final TriggerVisitor visitor) {
		visitor.visit(this);
	}

	public Date getDate() {
		return date;
	}

}
