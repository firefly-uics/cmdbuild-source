package org.cmdbuild.servlets.json.translationtable;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.data.store.lookup.Lookup;

import com.google.common.collect.Ordering;

enum LookupValueSorter {

	CODE("code") {
		@Override
		protected Ordering<Lookup> getOrdering() {
			return ORDER_LOOKUPVALUE_BY_CODE;
		}
	},
	DESCRIPTION("description") {
		@Override
		protected Ordering<Lookup> getOrdering() {
			return ORDER_LOOKUPVALUE_BY_DESCRIPTION;
		}
	},
	NUMBER("number") {
		@Override
		protected Ordering<Lookup> getOrdering() {
			return ORDER_LOOKUPVALUE_BY_NUMBER;
		}
	},
	UNDEFINED(StringUtils.EMPTY) {
		@Override
		protected Ordering<Lookup> getOrdering() {
			throw new UnsupportedOperationException();
		}
	};

	private final String sorter;

	private LookupValueSorter(final String sorter) {
		this.sorter = sorter;
	}

	abstract Ordering<Lookup> getOrdering();

	Ordering<Lookup> getOrdering(final String direction) {
		if (direction.equalsIgnoreCase("DESC")) {
			return getOrdering().reverse();
		} else {
			return getOrdering();
		}
	}

	static LookupValueSorter of(final String field) {
		for (final LookupValueSorter element : values()) {
			if (element.sorter.equalsIgnoreCase(field)) {
				return element;
			}
		}
		return UNDEFINED;
	}

	private static final Ordering<Lookup> ORDER_LOOKUPVALUE_BY_DESCRIPTION = new Ordering<Lookup>() {
		@Override
		public int compare(final Lookup left, final Lookup right) {
			return left.description().compareTo(right.description());
		}
	};

	private static final Ordering<Lookup> ORDER_LOOKUPVALUE_BY_NUMBER = new Ordering<Lookup>() {
		@Override
		public int compare(final Lookup left, final Lookup right) {
			return left.number().compareTo(right.number());
		}
	};

	private static final Ordering<Lookup> ORDER_LOOKUPVALUE_BY_CODE = new Ordering<Lookup>() {
		@Override
		public int compare(final Lookup left, final Lookup right) {
			return left.code().compareTo(right.code());
		}
	};

}
