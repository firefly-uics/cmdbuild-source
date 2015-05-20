package org.cmdbuild.data.store.lookup;

import com.google.common.base.Predicate;

public class Predicates {

	private static final Predicate<Lookup> ACTIVE = new Predicate<Lookup>() {

		@Override
		public boolean apply(final Lookup input) {
			return input.active();
		}

	};

	private static final Predicate<Lookup> DEFAULT = new Predicate<Lookup>() {

		@Override
		public boolean apply(final Lookup input) {
			return input.isDefault();
		}

	};

	public static Predicate<Lookup> lookupActive() {
		return ACTIVE;
	}

	public static Predicate<Lookup> lookupWithType(final LookupType type) {
		return new Predicate<Lookup>() {

			@Override
			public boolean apply(final Lookup input) {
				return input.type().name.equals(type.name);
			}

		};
	}

	public static Predicate<Lookup> lookupWithDescription(final String description) {
		return new Predicate<Lookup>() {

			@Override
			public boolean apply(final Lookup input) {
				return input.description().equals(description);
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

	public static Predicate<Lookup> defaultLookup() {
		return DEFAULT;
	}

	private Predicates() {
		// prevents instantiation
	}

}