package org.cmdbuild.service.rest.model.adapter;

import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_DESTINATION;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_ID;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_SOURCE;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_TYPE;
import static org.cmdbuild.service.rest.model.Builders.newRelation;

import java.util.Map;

import org.cmdbuild.service.rest.model.Relation;

import com.google.common.collect.Maps;

public class RelationAdapter extends ModelToMapAdapter<Relation> {

	@Override
	protected Map<String, Object> modelToMap(final Relation input) {
		final Map<String, Object> map = Maps.newHashMap();
		map.putAll(input.getValues());
		/*
		 * predefined attributes must always be added at last so they are not
		 * overwritten
		 */
		map.put(UNDERSCORED_TYPE, input.getType());
		map.put(UNDERSCORED_ID, input.getId());
		map.put(UNDERSCORED_SOURCE, input.getSource());
		map.put(UNDERSCORED_DESTINATION, input.getDestination());
		return map;
	}

	@Override
	protected Relation mapToModel(final Map<String, Object> input) {
		return newRelation() //
				.withType(getAndRemove(input, UNDERSCORED_TYPE, Long.class)) //
				.withId(getAndRemove(input, UNDERSCORED_ID, Long.class)) //
				.withValues(input) //
				.build();
	}

}
