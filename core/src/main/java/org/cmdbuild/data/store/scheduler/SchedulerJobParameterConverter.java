package org.cmdbuild.data.store.scheduler;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.data.store.DataViewStore.BaseStorableConverter;
import org.cmdbuild.model.scheduler.SchedulerJobParameter;

import com.google.common.collect.Maps;

public class SchedulerJobParameterConverter extends BaseStorableConverter<SchedulerJobParameter> {

	private static final String CLASSNAME = "_SchedulerJobParameter";

	private static final String SCHEDULER_ID = "SchedulerId";
	private static final String KEY = "Key";
	private static final String VALUE = "Value";

	private final Long schedulerId;

	public SchedulerJobParameterConverter(final Long schedulerId) {
		Validate.notNull(schedulerId, "scheduler's id cannot be null");
		Validate.isTrue(schedulerId > 0, "scheduler's id must be greater than zero");
		this.schedulerId = schedulerId;
	}

	@Override
	public String getClassName() {
		return CLASSNAME;
	}

	@Override
	public String getGroupAttributeName() {
		return SCHEDULER_ID;
	}

	@Override
	public Object getGroupAttributeValue() {
		return schedulerId;
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
