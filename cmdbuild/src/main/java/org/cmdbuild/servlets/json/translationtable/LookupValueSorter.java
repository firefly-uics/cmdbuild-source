package org.cmdbuild.servlets.json.translationtable;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.data.store.lookup.Lookup;

import com.google.common.collect.Ordering;

enum LookupValueSorter {

	CODE("code") {
		@Override
		protected Ordering<Lookup> getOrderingForProperty() {
			return ORDER_LOOKUPVALUE_BY_CODE;
		}
	},
	DESCRIPTION("description") {
		@Override
		protected Ordering<Lookup> getOrderingForProperty() {
			return ORDER_LOOKUPVALUE_BY_DESCRIPTION;
		}
	},
	NUMBER("number") {
		@Override
		protected Ordering<Lookup> getOrderingForProperty() {
			return ORDER_LOOKUPVALUE_BY_NUMBER;
		}
	},
	DEFAULT(StringUtils.EMPTY) {
		@Override
		protected Ordering<Lookup> getOrderingForProperty() {
			return DEFAULT_ORDER;
		}
	};

	private final String sorter;
	private String direction;

	private LookupValueSorter(final String sorter) {
		this.sorter = sorter;
	}

	public LookupValueSorter withDirection(final String direction) {
		this.direction = direction;
		return this;
	}

	abstract Ordering<Lookup> getOrderingForProperty();

	Ordering<Lookup> getOrientedOrdering() {
		direction = defaultIfBlank(direction, "ASC");
		if (direction.equalsIgnoreCase("DESC")) {
			return getOrderingForProperty().reverse();
		} else {
			return getOrderingForProperty();
		}
	}

	static LookupValueSorter of(final String field) {
		for (final LookupValueSorter element : values()) {
			if (element.sorter.equalsIgnoreCase(field)) {
				return element;
			}
		}
		return DEFAULT;
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

	private static final Ordering<Lookup> DEFAULT_ORDER = ORDER_LOOKUPVALUE_BY_NUMBER;

}
