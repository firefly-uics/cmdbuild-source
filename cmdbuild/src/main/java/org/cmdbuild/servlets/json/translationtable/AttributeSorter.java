package org.cmdbuild.servlets.json.translationtable;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.dao.entrytype.CMAttribute;

import com.google.common.collect.Ordering;

enum AttributeSorter {

	NAME("name") {
		@Override
		protected Ordering<CMAttribute> getOrderingForProperty() {
			return ORDER_ATTRIBUTE_BY_NAME;
		}
	},
	DESCRIPTION("description") {
		@Override
		protected Ordering<CMAttribute> getOrderingForProperty() {
			return ORDER_ATTRIBUTE_BY_DESCRIPTION;
		}
	},
	INDEX("index") {
		@Override
		protected Ordering<CMAttribute> getOrderingForProperty() {
			return ORDER_ATTRIBUTE_BY_INDEX;
		}
	},
	DEFAULT(StringUtils.EMPTY) {
		@Override
		protected Ordering<CMAttribute> getOrderingForProperty() {
			return DEFAULT_ORDER;
		}
	};

	private final String sorter;
	private String direction;

	private AttributeSorter(final String sorter) {
		this.sorter = sorter;
	}

	abstract Ordering<CMAttribute> getOrderingForProperty();

	public AttributeSorter withDirection(final String direction) {
		this.direction = direction;
		return this;
	}

	Ordering<CMAttribute> getOrientedOrdering() {
		direction = defaultIfBlank(direction, "ASC");
		if (direction.equalsIgnoreCase("DESC")) {
			return getOrderingForProperty().reverse();
		} else {
			return getOrderingForProperty();
		}
	}

	static AttributeSorter of(final String field) {
		for (final AttributeSorter element : values()) {
			if (element.sorter.equalsIgnoreCase(field)) {
				return element;
			}
		}
		return DEFAULT;
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

	private static final Ordering<CMAttribute> DEFAULT_ORDER = ORDER_ATTRIBUTE_BY_INDEX;

}
