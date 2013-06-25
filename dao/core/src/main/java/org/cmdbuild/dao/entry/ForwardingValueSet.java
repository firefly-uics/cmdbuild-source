package org.cmdbuild.dao.entry;

import java.util.Map.Entry;

public class ForwardingValueSet implements CMValueSet {

	private final CMValueSet inner;

	public ForwardingValueSet(final CMValueSet inner) {
		this.inner = inner;
	}

	@Override
	public Object get(final String key) {
		return inner.get(key);
	}

	@Override
	public <T> T get(final String key, final Class<? extends T> requiredType) {
		return inner.get(key, requiredType);
	}

	@Override
	public Iterable<Entry<String, Object>> getValues() {
		return inner.getValues();
	}

}
