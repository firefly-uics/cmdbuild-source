package org.cmdbuild.servlets.json.translationtable;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.data.store.lookup.LookupType;

import com.google.common.collect.Ordering;

enum LookupTypeSorter {

	DESCRIPTION("description") {
		@Override
		protected Ordering<LookupType> getOrdering() {
			return ORDER_LOOKUPTYPE_BY_DESCRIPTION;
		}
	},
	UNDEFINED(StringUtils.EMPTY) {
		@Override
		protected Ordering<LookupType> getOrdering() {
			throw new UnsupportedOperationException();
		}
	};

	private final String sorter;

	private LookupTypeSorter(final String sorter) {
		this.sorter = sorter;
	}

	abstract Ordering<LookupType> getOrdering();

	Ordering<LookupType> getOrdering(final String direction) {
		if (direction.equalsIgnoreCase("DESC")) {
			return getOrdering().reverse();
		} else {
			return getOrdering();
		}
	}

	static LookupTypeSorter of(final String field) {
		for (final LookupTypeSorter element : values()) {
			if (element.sorter.equalsIgnoreCase(field)) {
				return element;
			}
		}
		return UNDEFINED;
	}
	
	/*
	 * what the client calls 'description' for the server is 'name'
	 */
	private static final Ordering<LookupType> ORDER_LOOKUPTYPE_BY_DESCRIPTION = new Ordering<LookupType>() {
		@Override
		public int compare(final LookupType left, final LookupType right) {
			return left.name.compareTo(right.name);
		}
	};

}
