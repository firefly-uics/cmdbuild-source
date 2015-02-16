package org.cmdbuild.data.store;

import java.util.NoSuchElementException;

public class Stores {

	/**
	 * This store returns {@code null} instead of throwing
	 * {@link NoSuchElementException} when {@link #read(Storable)} fails.
	 */
	private static class NullOnNotFoundReadStore<T extends Storable> extends ForwardingStore<T> {

		public NullOnNotFoundReadStore(final Store<T> inner) {
			super(inner);
		}

		/**
		 * @return {@code null} when no or more than once {@link Storable}
		 *         elements are found.
		 */
		@Override
		public T read(final Storable storable) {
			try {
				return super.read(storable);
			} catch (final NoSuchElementException e) {
				return null;
			}
		}

	}

	/**
	 * This store returns {@code null} instead of throwing
	 * {@link NoSuchElementException} when {@link #read(Storable)} fails.
	 */
	public static <T extends Storable> Store<T> nullOnNotFoundRead(final Store<T> store) {
		return new NullOnNotFoundReadStore<T>(store);
	}

	private Stores() {
		// prevents instantiation
	}

}
