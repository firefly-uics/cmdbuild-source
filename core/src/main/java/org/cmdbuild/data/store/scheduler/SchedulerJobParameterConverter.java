package org.cmdbuild.data.store.scheduler;

import static org.cmdbuild.data.store.scheduler.SchedulerJobParameterConstants.CLASSNAME;
import static org.cmdbuild.data.store.scheduler.SchedulerJobParameterConstants.KEY;
import static org.cmdbuild.data.store.scheduler.SchedulerJobParameterConstants.SCHEDULER_ID;
import static org.cmdbuild.data.store.scheduler.SchedulerJobParameterConstants.VALUE;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.DataViewStore.BaseStorableConverter;

import com.google.common.collect.Maps;

public class SchedulerJobParameterConverter extends BaseStorableConverter<SchedulerJobParameter> {

	@Override
	public String getClassName() {
		return CLASSNAME;
	}

	@Override
	public SchedulerJobParameter convert(final CMCard card) {
		return SchedulerJobParameter.newInstance() //
				.withId(card.getId()) //
				.withOwner(card.get(SCHEDULER_ID, Long.class)) //
				.withKey(card.get(KEY, String.class)) //
				.withValue(card.get(VALUE, String.class)) //
				.build();
	}

	@Override
	public Map<String, Object> getValues(final SchedulerJobParameter storable) {
		final Map<String, Object> values = Maps.newHashMap();
		values.put(SCHEDULER_ID, storable.getId());
		values.put(KEY, storable.getKey());
		values.put(VALUE, storable.getValue());
		return values;
	}

}
