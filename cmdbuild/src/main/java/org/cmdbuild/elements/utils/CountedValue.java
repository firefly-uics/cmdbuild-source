package org.cmdbuild.elements.utils;

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
