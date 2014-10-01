package org.cmdbuild.service.rest.model.adapter;

import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_ID;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_TYPE;
import static org.cmdbuild.service.rest.model.Builders.newCard;

import java.util.Map;

import org.cmdbuild.service.rest.model.Card;

import com.google.common.collect.Maps;

public class CardAdapter extends ModelToMapAdapter<Card> {

	@Override
	protected Map<String, Object> modelToMap(final Card input) {
		final Map<String, Object> map = Maps.newHashMap();
		map.putAll(input.getValues());
		/*
		 * predefined attributes must always be added at last so they are not
		 * overwritten
		 */
		map.put(UNDERSCORED_TYPE, input.getType());
		map.put(UNDERSCORED_ID, input.getId());
		return map;
	}

	@Override
	protected Card mapToModel(final Map<String, Object> input) {
		return newCard() //
				.withType(getAndRemove(input, UNDERSCORED_TYPE, Long.class)) //
				.withId(getAndRemove(input, UNDERSCORED_ID, Long.class)) //
				.withValues(input) //
				.build();
	}

}
