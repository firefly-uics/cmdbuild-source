package org.cmdbuild.scheduler.quartz;

import java.text.ParseException;

import org.cmdbuild.scheduler.OneTimeTrigger;
import org.cmdbuild.scheduler.RecurringTrigger;
import org.cmdbuild.scheduler.SchedulerExeptionFactory;
import org.cmdbuild.scheduler.SchedulerTrigger;
import org.cmdbuild.scheduler.TriggerVisitor;
import org.quartz.CronTrigger;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

public class QuartzTriggerFactory implements TriggerVisitor {

	private final SchedulerExeptionFactory exeptionFactory;

	private Trigger quartzTrigger;

	public QuartzTriggerFactory(final SchedulerExeptionFactory exeptionFactory) {
		this.exeptionFactory = exeptionFactory;
	}

	public Trigger create(final SchedulerTrigger trigger) {
		trigger.accept(this);
		return quartzTrigger;
	}

	@Override
	public void visit(final OneTimeTrigger trigger) {
		quartzTrigger = new SimpleTrigger(String.format("onetimetrigger%d", this.hashCode()), trigger.getDate());
	}

	@Override
	public void visit(final RecurringTrigger trigger) {
		final CronTrigger cronTrigger = new CronTrigger(String.format("crontrigger%d", this.hashCode()));
		final String cronExpression = trigger.getCronExpression();
		try {
			cronTrigger.setCronExpression(cronExpression);
		} catch (final ParseException e) {
			throw exeptionFactory.cronExpression(e, cronExpression);
		}
		quartzTrigger = cronTrigger;
	}

}
