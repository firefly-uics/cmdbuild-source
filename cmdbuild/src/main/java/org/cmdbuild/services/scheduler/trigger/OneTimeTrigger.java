package org.cmdbuild.services.scheduler.trigger;

import java.util.Date;



public class OneTimeTrigger extends JobTrigger {

	Date date;

	public OneTimeTrigger(Date date) {
		this.date = date;
	}

	public Date getDate() {
		return date;
	}

	public Object accept(TriggerVisitor factory) {
		return factory.visit(this);
	}
}
