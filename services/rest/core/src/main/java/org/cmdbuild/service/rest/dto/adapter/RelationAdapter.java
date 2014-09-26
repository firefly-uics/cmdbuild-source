package org.cmdbuild.service.rest.dto.adapter;

import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_DESTINATION;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_ID;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_SOURCE;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_TYPE;
import static org.cmdbuild.service.rest.dto.Builders.newRelation;

import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.ObjectUtils;
import org.cmdbuild.service.rest.dto.Relation;

import com.google.common.collect.Maps;

public class RelationAdapter extends XmlAdapter<Map<String, Object>, Relation> {

	@Override
	public Map<String, Object> marshal(final Relation input) throws Exception {
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
	public Relation unmarshal(final Map<String, Object> input) throws Exception {
		return newRelation() //
				.withType(getAndRemove(input, UNDERSCORED_TYPE, String.class)) //
				.withId(getAndRemove(input, UNDERSCORED_ID, Long.class)) //
				.withValues(input) //
				.build();
	}

	private <T> T getAndRemove(final Map<String, Object> mapType, final String key, final Class<T> type) {
		for (final Entry<String, Object> element : mapType.entrySet()) {
			if (ObjectUtils.equals(element.getKey(), key)) {
				final Object value = element.getValue();
				mapType.remove(key);
				return type.cast(value);
			}
		}
		return null;
	}

}
