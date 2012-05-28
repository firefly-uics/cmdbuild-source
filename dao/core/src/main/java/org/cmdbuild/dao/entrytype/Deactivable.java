package org.cmdbuild.dao.entrytype;

import static com.google.common.collect.Iterables.filter;

import com.google.common.base.Predicate;

public interface Deactivable {

	boolean isActive();

	class IsActivePredicate implements Predicate<Deactivable> {

		private static final IsActivePredicate INSTANCE = new IsActivePredicate();

		@Override
		public boolean apply(Deactivable input) {
			return input.isActive();
		}

		private static final IsActivePredicate thatAreActive() {
			return INSTANCE;
		}

		public static <T extends Deactivable> Iterable<T> filterActive(Iterable<T> unfiltered) {
			return filter(unfiltered, thatAreActive());
		}
	}
}
