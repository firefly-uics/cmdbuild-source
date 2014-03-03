package org.cmdbuild.data.store.scheduler;

import static org.cmdbuild.data.store.scheduler.SchedulerJobParameterConstants.CLASSNAME;
import static org.cmdbuild.data.store.scheduler.SchedulerJobParameterConstants.KEY;
import static org.cmdbuild.data.store.scheduler.SchedulerJobParameterConstants.SCHEDULER_ID;
import static org.cmdbuild.data.store.scheduler.SchedulerJobParameterConstants.VALUE;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.DataViewStore.BaseStorableConverter;

import com.google.common.collect.Maps;

public class SchedulerJobParameterConverter extends BaseStorableConverter<SchedulerJobParameter> {

	public static SchedulerJobParameterConverter of(final SchedulerJob schedulerJob) {
		return new SchedulerJobParameterConverter(schedulerJob.getId());
	}

	private final Long schedulerId;

	private SchedulerJobParameterConverter(final Long schedulerId) {
		Validate.notNull(schedulerId, "scheduler's id cannot be null");
		Validate.isTrue(schedulerId > 0, "scheduler's id must be greater than zero");
		this.schedulerId = schedulerId;
	}

	@Override
	public String getClassName() {
		return CLASSNAME;
	}

	@Override
	public SchedulerJobParameter convert(final CMCard card) {
		return SchedulerJobParameter.newInstance() //
				.withId(card.getId()) //
				.withKey(card.get(KEY, String.class)) //
				.withValue(card.get(VALUE, String.class)) //
				.build();
	}

	@Override
	public Map<String, Object> getValues(final SchedulerJobParameter storable) {
		final Map<String, Object> values = Maps.newHashMap();
		values.put(SCHEDULER_ID, schedulerId);
		values.put(KEY, storable.getKey());
		values.put(VALUE, storable.getValue());
		return values;
	}

}
