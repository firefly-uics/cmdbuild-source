package org.cmdbuild.data.store.lookup;

import com.google.common.base.Predicate;

public class Predicates {

	public static Predicate<Lookup> lookupWithType(final LookupType type) {
		return new Predicate<Lookup>() {

			@Override
			public boolean apply(final Lookup input) {
				return input.type.equals(type);
			}

		};
	}

	public static Predicate<Lookup> lookupWithDescription(final String description) {
		return new Predicate<Lookup>() {

			@Override
			public boolean apply(final Lookup input) {
				return input.description.equals(description);
			}

		};
	}

	public static Predicate<LookupType> lookupTypeWithName(final String name) {
		return new Predicate<LookupType>() {

			@Override
			public boolean apply(final LookupType input) {
				return input.name.equals(name);
			}

		};
	}

	private Predicates() {
		// prevents instantiation
	}

}
