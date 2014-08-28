package org.cmdbuild.service.rest.dto.adapter;

import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_NAME;

import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.ObjectUtils;
import org.cmdbuild.service.rest.dto.ProcessInstance;

import com.google.common.collect.Maps;

public class ProcessInstanceAdapter extends XmlAdapter<Map<String, Object>, ProcessInstance> {

	@Override
	public Map<String, Object> marshal(final ProcessInstance input) throws Exception {
		final Map<String, Object> map = Maps.newHashMap();
		map.putAll(input.getValues());
		/*
		 * predefined attributes must always be added at last so they are not
		 * overwritten
		 */
		map.put(UNDERSCORED_NAME, input.getName());
		return map;
	}

	@Override
	public ProcessInstance unmarshal(final Map<String, Object> input) throws Exception {
		return ProcessInstance.newInstance() //
				.withName(getAndRemove(input, UNDERSCORED_NAME, String.class)) //
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
