package org.cmdbuild.services.sync.store;

import java.util.Map;

public interface Entry<T extends Type> {

	T getType();

	Iterable<Map.Entry<String, Object>> getValues();

	Object getValue(String name);

	Key getKey();

}
