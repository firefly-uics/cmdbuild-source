package org.cmdbuild.data.store.scheduler;

import static org.cmdbuild.data.store.scheduler.SchedulerJobParameterConstants.SCHEDULER_ID;

import org.apache.commons.lang.Validate;
import org.cmdbuild.data.store.Groupable;

public class SchedulerJobParameterGroupable implements Groupable {

	public static SchedulerJobParameterGroupable of(final SchedulerJob schedulerJob) {
		return new SchedulerJobParameterGroupable(schedulerJob);
	}

	private final SchedulerJob schedulerJob;

	private SchedulerJobParameterGroupable(final SchedulerJob schedulerJob) {
		Validate.notNull(schedulerJob, "scheduler job cannot be null");
		this.schedulerJob = schedulerJob;
	}

	@Override
	public String getGroupAttributeName() {
		return SCHEDULER_ID;
	}

	@Override
	public Object getGroupAttributeValue() {
		return schedulerJob.getId();
	}

}