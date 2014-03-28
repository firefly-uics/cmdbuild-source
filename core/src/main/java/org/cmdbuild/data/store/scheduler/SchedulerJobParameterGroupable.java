package org.cmdbuild.data.store.scheduler;

import static org.cmdbuild.data.store.scheduler.SchedulerJobParameterConstants.SCHEDULER_ID;

import org.apache.commons.lang.Validate;
import org.cmdbuild.data.store.Groupable;

public class SchedulerJobParameterGroupable implements Groupable {

	public static SchedulerJobParameterGroupable of(final Long schedulerId) {
		return new SchedulerJobParameterGroupable(schedulerId);
	}

	private final Long schedulerId;

	private SchedulerJobParameterGroupable(final Long schedulerId) {
		Validate.notNull(schedulerId, "scheduler's id cannot be null");
		Validate.isTrue(schedulerId > 0, "scheduler's id must be greater than zero");
		this.schedulerId = schedulerId;
	}

	@Override
	public String getGroupAttributeName() {
		return SCHEDULER_ID;
	}

	@Override
	public Object getGroupAttributeValue() {
		return schedulerId;
	}

}