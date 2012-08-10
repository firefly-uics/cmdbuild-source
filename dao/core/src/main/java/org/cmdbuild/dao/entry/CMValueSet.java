package org.cmdbuild.dao.entry;

import java.util.Map;

/**
 * Immutable value set
 */
public interface CMValueSet {

	public Object get(final String key);
	public Iterable<Map.Entry<String, Object>> getValues();
}
