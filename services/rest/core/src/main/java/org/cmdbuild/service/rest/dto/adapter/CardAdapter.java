package org.cmdbuild.service.rest.dto.adapter;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_ID;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_TYPE;
import static org.cmdbuild.service.rest.dto.Builders.newCard;

import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.ObjectUtils;
import org.cmdbuild.service.rest.dto.Card;

import com.google.common.collect.Maps;

public class CardAdapter extends XmlAdapter<Map<String, Object>, Card> {

	@Override
	public Map<String, Object> marshal(final Card input) throws Exception {
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
	public Card unmarshal(final Map<String, Object> input) throws Exception {
		return newCard() //
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
				final Object _value;
				if (Long.class.equals(type)) {
					String s;
					if (value instanceof Long) {
						s = Long.class.cast(value).toString();
					} else if (value instanceof Integer) {
						s = Integer.class.cast(value).toString();
					} else {
						s = value.toString();
					}
					s = String.class.cast(s);
					_value = isBlank(s) ? null : Long.parseLong(s);
				} else {
					_value = value;
				}
				return type.cast(_value);
			}
		}
		return null;
	}

}
