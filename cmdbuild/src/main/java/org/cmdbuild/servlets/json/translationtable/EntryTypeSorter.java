package org.cmdbuild.servlets.json.translationtable;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import org.cmdbuild.dao.entrytype.CMEntryType;

import com.google.common.collect.Ordering;

enum EntryTypeSorter {
	NAME("name") {
		@Override
		protected Ordering<CMEntryType> getOrderingForProperty() {
			return ORDER_ENTRYTYPE_BY_NAME;
		}
	},
	DESCRIPTION("description") {
		@Override
		protected Ordering<CMEntryType> getOrderingForProperty() {
			return ORDER_ENTRYTYPE_BY_DESCRIPTION;
		}
	},
	DEFAULT(EMPTY) {
		@Override
		protected Ordering<CMEntryType> getOrderingForProperty() {
			return DEFAULT_ORDER;
		}
	};

	private final String sorter;
	private String direction;

	private EntryTypeSorter(final String sorter) {
		this.sorter = sorter;
	}

	EntryTypeSorter withDirection(final String direction) {
		this.direction = direction;
		return this;
	}

	abstract Ordering<CMEntryType> getOrderingForProperty();

	Ordering<CMEntryType> getOrientedOrdering() {
		direction = defaultIfBlank(direction, "ASC");
		if (direction.equalsIgnoreCase("DESC")) {
			return getOrderingForProperty().reverse();
		} else {
			return getOrderingForProperty();
		}
	}

	static EntryTypeSorter of(final String field) {
		for (final EntryTypeSorter element : values()) {
			if (element.sorter.equalsIgnoreCase(field)) {
				return element;
			}
		}
		return DEFAULT;
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

	private static final Ordering<CMEntryType> DEFAULT_ORDER = ORDER_ENTRYTYPE_BY_DESCRIPTION;

}
