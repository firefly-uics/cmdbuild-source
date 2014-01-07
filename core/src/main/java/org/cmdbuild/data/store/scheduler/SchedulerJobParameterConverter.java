package org.cmdbuild.data.store.scheduler;

import static org.cmdbuild.data.store.scheduler.SchedulerJobParameterConstants.CLASSNAME;
import static org.cmdbuild.data.store.scheduler.SchedulerJobParameterConstants.KEY;
import static org.cmdbuild.data.store.scheduler.SchedulerJobParameterConstants.SCHEDULER_ID;
import static org.cmdbuild.data.store.scheduler.SchedulerJobParameterConstants.VALUE;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.DataViewStore.BaseStorableConverter;
import org.cmdbuild.model.scheduler.SchedulerJobParameter;

import com.google.common.collect.Maps;

public class SchedulerJobParameterConverter extends BaseStorableConverter<SchedulerJobParameter> {

	public static SchedulerJobParameterConverter of(final Long schedulerId) {
		return new SchedulerJobParameterConverter(schedulerId);
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
		final SchedulerJobParameter storable = new SchedulerJobParameter(card.getId());
		storable.setKey(card.get(KEY, String.class));
		storable.setValue(card.get(VALUE, String.class));
		return storable;
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
