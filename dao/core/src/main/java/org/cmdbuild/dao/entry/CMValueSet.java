package org.cmdbuild.dao.entry;

import java.util.Map;

/**
 * Immutable value set
 */
public interface CMValueSet {

	Object get(String key);

	<T> T get(String key, Class<? extends T> requiredType);

	Iterable<Map.Entry<String, Object>> getValues();

}
