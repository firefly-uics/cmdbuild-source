package org.cmdbuild.service.rest.model.adapter;

import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_DESTINATION_CODE;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_DESTINATION_ID;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_DESTINATION_TYPE;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_ID;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_SOURCE_CODE;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_SOURCE_DESCRIPTION;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_SOURCE_ID;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_SOURCE_TYPE;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_TYPE;
import static org.cmdbuild.service.rest.model.Models.newRelation;

import java.util.Map;

import org.cmdbuild.service.rest.model.Card;
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
		final Card source = input.getSource();
		map.put(UNDERSCORED_SOURCE_ID, source.getId());
		map.put(UNDERSCORED_SOURCE_TYPE, source.getType());
		final Map<String, Object> sourceValues = source.getValues();
		map.put(UNDERSCORED_SOURCE_CODE, sourceValues.get(CODE_ATTRIBUTE));
		map.put(UNDERSCORED_SOURCE_DESCRIPTION, sourceValues.get(DESCRIPTION_ATTRIBUTE));
		final Card destination = input.getDestination();
		map.put(UNDERSCORED_DESTINATION_ID, destination.getId());
		map.put(UNDERSCORED_DESTINATION_TYPE, destination.getType());
		final Map<String, Object> destinationValues = destination.getValues();
		map.put(UNDERSCORED_DESTINATION_CODE, destinationValues.get(CODE_ATTRIBUTE));
		map.put(UNDERSCORED_DESTINATION_CODE, destinationValues.get(DESCRIPTION_ATTRIBUTE));
		return map;
	}

	@Override
	protected Relation mapToModel(final Map<String, Object> input) {
		return newRelation() //
				.withType(getAndRemove(input, UNDERSCORED_TYPE, String.class)) //
				.withId(getAndRemove(input, UNDERSCORED_ID, Long.class)) //
				.withValues(input) //
				.build();
	}

}
