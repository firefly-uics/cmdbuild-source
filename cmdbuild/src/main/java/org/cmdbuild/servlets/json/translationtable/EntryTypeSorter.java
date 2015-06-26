package org.cmdbuild.servlets.json.translationtable;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.dao.entrytype.CMEntryType;

import com.google.common.collect.Ordering;

enum EntryTypeSorter {
	NAME("name") {
		@Override
		protected Ordering<CMEntryType> getOrdering() {
			return ORDER_ENTRYTYPE_BY_NAME;
		}
	},
	DESCRIPTION("description") {
		@Override
		protected Ordering<CMEntryType> getOrdering() {
			return ORDER_ENTRYTYPE_BY_DESCRIPTION;
		}
	},
	UNDEFINED(StringUtils.EMPTY) {
		@Override
		protected Ordering<CMEntryType> getOrdering() {
			throw new UnsupportedOperationException();
		}
	};

	private final String sorter;

	private EntryTypeSorter(final String sorter) {
		this.sorter = sorter;
	}

	abstract Ordering<CMEntryType> getOrdering();

	Ordering<CMEntryType> getOrdering(final String direction) {
		if (direction.equalsIgnoreCase("DESC")) {
			return getOrdering().reverse();
		} else {
			return getOrdering();
		}
	}

	static EntryTypeSorter of(final String field) {
		for (final EntryTypeSorter element : values()) {
			if (element.sorter.equalsIgnoreCase(field)) {
				return element;
			}
		}
		return UNDEFINED;
	}

	private static final Ordering<CMEntryType> ORDER_ENTRYTYPE_BY_NAME = new Ordering<CMEntryType>() {
		@Override
		public int compare(final CMEntryType left, final CMEntryType right) {
			return left.getName().compareTo(right.getName());
		}
	};

	private static final Ordering<CMEntryType> ORDER_ENTRYTYPE_BY_DESCRIPTION = new Ordering<CMEntryType>() {
		@Override
		public int compare(final CMEntryType left, final CMEntryType right) {
			return left.getDescription().compareTo(right.getDescription());
		}
	};

}
