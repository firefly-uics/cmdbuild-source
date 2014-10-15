package org.cmdbuild.service.rest.model.adapter;

import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_ACTIVITY;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_ADVANCE;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_ID;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_NAME;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_TYPE;
import static org.cmdbuild.service.rest.model.Builders.newProcessInstanceAdvance;

import java.util.Map;

import org.cmdbuild.service.rest.model.ProcessInstanceAdvanceable;

import com.google.common.collect.Maps;

public class ProcessInstanceAdvanceableAdapter extends ModelToMapAdapter<ProcessInstanceAdvanceable> {

	@Override
	protected Map<String, Object> modelToMap(final ProcessInstanceAdvanceable input) {
		final Map<String, Object> map = Maps.newHashMap();
		map.putAll(input.getValues());
		/*
		 * predefined attributes must always be added at last so they are not
		 * overwritten
		 */
		map.put(UNDERSCORED_TYPE, input.getType());
		map.put(UNDERSCORED_ID, input.getId());
		map.put(UNDERSCORED_NAME, input.getName());
		map.put(UNDERSCORED_ACTIVITY, input.getActivity());
		map.put(UNDERSCORED_ADVANCE, input.isAdvance());
		return map;
	}

	@Override
	protected ProcessInstanceAdvanceable mapToModel(final Map<String, Object> input) {
		return newProcessInstanceAdvance() //
				.withType(getAndRemove(input, UNDERSCORED_TYPE, String.class)) //
				.withId(getAndRemove(input, UNDERSCORED_ID, Long.class)) //
				.withName(getAndRemove(input, UNDERSCORED_NAME, String.class)) //
				.withActivity(getAndRemove(input, UNDERSCORED_ACTIVITY, String.class)) //
				.withAdvance(getAndRemove(input, UNDERSCORED_ADVANCE, Boolean.class)) //
				.withValues(input) //
				.build();
	}

}
