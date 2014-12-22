package org.cmdbuild.service.rest.model.adapter;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang3.ObjectUtils;
import org.cmdbuild.service.rest.model.Model;

public abstract class ModelToMapAdapter<T extends Model> extends XmlAdapter<Map<String, Object>, T> {

	protected static <T> T getAndRemove(final Map<String, Object> mapType, final String key, final Class<T> type) {
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

	@Override
	public final Map<String, Object> marshal(final T v) throws Exception {
		return modelToMap(v);
	}

	protected abstract Map<String, Object> modelToMap(T input);

	@Override
	public final T unmarshal(final Map<String, Object> v) throws Exception {
		return mapToModel(v);
	}

	protected abstract T mapToModel(Map<String, Object> input);

}