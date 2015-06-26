package org.cmdbuild.servlets.json.translationtable;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.dao.entrytype.CMAttribute;

import com.google.common.collect.Ordering;

enum AttributeSorter {

	NAME("name") {
		@Override
		protected Ordering<CMAttribute> getOrdering() {
			return ORDER_ATTRIBUTE_BY_NAME;
		}
	},
	DESCRIPTION("description") {
		@Override
		protected Ordering<CMAttribute> getOrdering() {
			return ORDER_ATTRIBUTE_BY_DESCRIPTION;
		}
	},
	INDEX("index") {
		@Override
		protected Ordering<CMAttribute> getOrdering() {
			return ORDER_ATTRIBUTE_BY_INDEX;
		}
	},
	UNDEFINED(StringUtils.EMPTY) {
		@Override
		protected Ordering<CMAttribute> getOrdering() {
			throw new UnsupportedOperationException();
		}
	};

	private final String sorter;

	private AttributeSorter(final String sorter) {
		this.sorter = sorter;
	}

	abstract Ordering<CMAttribute> getOrdering();

	Ordering<CMAttribute> getOrdering(final String direction) {
		if (direction.equalsIgnoreCase("DESC")) {
			return getOrdering().reverse();
		} else {
			return getOrdering();
		}
	}

	static AttributeSorter of(final String field) {
		for (final AttributeSorter element : values()) {
			if (element.sorter.equalsIgnoreCase(field)) {
				return element;
			}
		}
		return UNDEFINED;
	}

	private static final Ordering<CMAttribute> ORDER_ATTRIBUTE_BY_NAME = new Ordering<CMAttribute>() {
		@Override
		public int compare(final CMAttribute left, final CMAttribute right) {
			return left.getName().compareTo(right.getName());
		}
	};

	private static final Ordering<CMAttribute> ORDER_ATTRIBUTE_BY_DESCRIPTION = new Ordering<CMAttribute>() {
		@Override
		public int compare(final CMAttribute left, final CMAttribute right) {
			return left.getDescription().compareTo(right.getDescription());
		}
	};

	private static final Ordering<CMAttribute> ORDER_ATTRIBUTE_BY_INDEX = new Ordering<CMAttribute>() {
		@Override
		public int compare(final CMAttribute left, final CMAttribute right) {
			return left.getIndex() > right.getIndex() ? +1 : left.getIndex() < right.getIndex() ? -1 : 0;
		}
	};

}
