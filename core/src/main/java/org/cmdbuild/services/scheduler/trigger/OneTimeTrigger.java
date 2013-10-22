package org.cmdbuild.services.scheduler.trigger;

import java.util.Date;

public class OneTimeTrigger extends JobTrigger {

	Date date;

	public OneTimeTrigger(final Date date) {
		this.date = date;
	}

	public Date getDate() {
		return date;
	}

	@Override
	public Object accept(final TriggerVisitor factory) {
		return factory.visit(this);
	}
}
