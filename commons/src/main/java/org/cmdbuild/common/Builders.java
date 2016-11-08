package org.cmdbuild.common;

public class Builders {

	private static class Identity<T> implements Builder<T> {

		private final T value;

		public Identity(final T value) {
			this.value = value;
		}

		@Override
		public T build() {
			return value;
		}

	}

	public static <T> Builder<T> identity(final T value) {
		return new Identity<T>(value);
	}

	private Builders() {
		// prevents instantiation
	}

}
