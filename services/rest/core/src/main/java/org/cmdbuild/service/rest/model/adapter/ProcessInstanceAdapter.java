package org.cmdbuild.service.rest.model.adapter;

import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_ID;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_NAME;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_TYPE;
import static org.cmdbuild.service.rest.model.Builders.newProcessInstance;

import java.util.Map;

import org.cmdbuild.service.rest.model.ProcessInstance;

import com.google.common.collect.Maps;

public class ProcessInstanceAdapter extends ModelToMapAdapter<ProcessInstance> {

	@Override
	protected Map<String, Object> modelToMap(final ProcessInstance input) {
		final Map<String, Object> map = Maps.newHashMap();
		map.putAll(input.getValues());
		/*
		 * predefined attributes must always be added at last so they are not
		 * overwritten
		 */
		map.put(UNDERSCORED_TYPE, input.getType());
		map.put(UNDERSCORED_ID, input.getId());
		map.put(UNDERSCORED_NAME, input.getName());
		return map;
	}

	@Override
	protected ProcessInstance mapToModel(final Map<String, Object> input) {
		return newProcessInstance() //
				.withType(getAndRemove(input, UNDERSCORED_TYPE, String.class)) //
				.withId(getAndRemove(input, UNDERSCORED_ID, Long.class)) //
				.withName(getAndRemove(input, UNDERSCORED_NAME, String.class)) //
				.withValues(input) //
				.build();
	}

}
