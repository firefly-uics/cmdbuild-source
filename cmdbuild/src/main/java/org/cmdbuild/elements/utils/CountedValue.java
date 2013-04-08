package org.cmdbuild.elements.utils;

import org.cmdbuild.common.annotations.OldDao;

@OldDao
@Deprecated
public class CountedValue<V> {
	private int count;
	private V value;

	public CountedValue(int count, V value) {
		this.count = count;
		this.value = value;
	}

	public int getCount() {
		return count;
	}

	public V getValue() {
		return value;
	}
}
