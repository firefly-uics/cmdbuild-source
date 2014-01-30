package org.cmdbuild.services.scheduler.quartz;

import java.text.ParseException;

import org.cmdbuild.exception.SchedulerException.SchedulerExceptionType;
import org.cmdbuild.services.scheduler.trigger.JobTrigger;
import org.cmdbuild.services.scheduler.trigger.OneTimeTrigger;
import org.cmdbuild.services.scheduler.trigger.RecurringTrigger;
import org.cmdbuild.services.scheduler.trigger.TriggerVisitor;
import org.quartz.CronTrigger;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

public class QuartzTriggerFactory implements TriggerVisitor {

	public Trigger create(final JobTrigger trigger) {
		return (Trigger) trigger.accept(this);
	}

	@Override
	public Object visit(final OneTimeTrigger trigger) {
		return new SimpleTrigger(String.format("onetimetrigger%d", this.hashCode()), trigger.getDate());
	}

	@Override
	public Object visit(final RecurringTrigger trigger) {
		final CronTrigger cronTrigger = new CronTrigger(String.format("crontrigger%d", this.hashCode()));
		final String cronExpression = trigger.getCronExpression();
		try {
			cronTrigger.setCronExpression(cronExpression);
		} catch (final ParseException e) {
			throw SchedulerExceptionType.ILLEGAL_CRON_EXPRESSION.createException(cronExpression);
		}
		return cronTrigger;
	}
}
