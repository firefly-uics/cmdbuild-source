package org.cmdbuild.connector.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;

public class Key implements Comparable<Key> {

	private final List<String> values;

	public Key(final KeyValue... values) {
		this((values == null) ? null : Arrays.asList(values));
	}

	public Key(final List<KeyValue> values) {
		Validate.notNull(values, "null values");
		Validate.notEmpty(values, "empty values");
		this.values = new ArrayList<String>();
		for (final KeyValue value : values) {
			this.values.addAll(value.getValues());
		}
	}

	@Override
	public int compareTo(final Key key) {
		final int count = Math.min(values.size(), key.values.size());
		for (int i = 0; i < count; i++) {
			final int compare = values.get(i).compareTo(key.values.get(i));
			if (compare != 0) {
				return compare;
			}
		}
		if (values.size() == key.values.size()) {
			return 0;
		} else {
			return (values.size() < key.values.size() ? -1 : +1);
		}
	}

	@Override
	public boolean equals(final Object object) {
		if (object == null) {
			return false;
		}

		if (object instanceof Key) {
			final Key key = Key.class.cast(object);
			return (compareTo(key) == 0);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return values.hashCode();
	}

	@Override
	public String toString() {
		return String.format("[values:%s]", Arrays.toString(values.toArray()));
	}

	public static KeyValue createKeyValue(final String... values) {
		return new KeyValueImpl(values);
	}

	public static KeyValue createKeyValue(final Key key) {
		return new KeyValueImpl(key.values);
	}

	private static class KeyValueImpl implements KeyValue {

		protected final List<String> values;

		protected KeyValueImpl(final String... values) {
			this((values == null) ? null : Arrays.asList(values));
		}

		protected KeyValueImpl(final List<String> values) {
			Validate.notNull(values, "null values");
			Validate.notEmpty(values, "empty values");
			this.values = new ArrayList<String>(values);
		}

		@Override
		public List<String> getValues() {
			return values;
		}

	}

}
