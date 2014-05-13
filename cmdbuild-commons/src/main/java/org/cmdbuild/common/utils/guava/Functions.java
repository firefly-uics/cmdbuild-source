package org.cmdbuild.common.utils.guava;

import java.util.Map.Entry;

import com.google.common.base.Function;

public final class Functions {

	private enum StringFunctions implements Function<String, String> {

		TRIM {

			@Override
			protected String doApply(final String input) {
				return input.trim();
			}

		}, //
		;

		@Override
		public String apply(final String input) {
			return doApply(input);
		}

		protected abstract String doApply(final String input);

	}

	private static class ToKeyFunction<K, V> implements Function<Entry<K, V>, K> {

		public static <K, V> ToKeyFunction<K, V> getInstance() {
			return new ToKeyFunction<K, V>();
		}

		private ToKeyFunction() {
			// use factory method
		}

		@Override
		public K apply(final Entry<K, V> input) {
			return input.getKey();
		}

	}

	private static class ToValueFunction<K, V> implements Function<Entry<K, V>, V> {

		public static <K, V> ToValueFunction<K, V> getInstance() {
			return new ToValueFunction<K, V>();
		}

		private ToValueFunction() {
			// use factory method
		}

		@Override
		public V apply(final Entry<K, V> input) {
			return input.getValue();
		}

	}

	public static Function<String, String> trim() {
		return StringFunctions.TRIM;
	}

	public static <K, V> Function<Entry<K, V>, K> toKey() {
		return ToKeyFunction.getInstance();
	}

	public static <K, V> Function<Entry<K, V>, V> toValue() {
		return ToValueFunction.getInstance();
	}

	private Functions() {
		// prevents instantiation
	}

}
