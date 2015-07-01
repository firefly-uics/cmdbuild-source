package org.cmdbuild.servlets.json.translationtable;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.auth.acl.CMGroup;

import com.google.common.collect.Ordering;

enum MenuSorter {

	GROUPNAME("name") {
		@Override
		protected Ordering<CMGroup> getOrderingForProperty() {
			return ORDER_BY_GROUP_NAME;
		}
	},
	GROUPDESCRIPTION("description") {
		@Override
		protected Ordering<CMGroup> getOrderingForProperty() {
			return ORDER_BY_GROUP_DESCRIPTION;
		}
	},
	DEFAULT(StringUtils.EMPTY) {
		@Override
		protected Ordering<CMGroup> getOrderingForProperty() {
			return DEFAULT_ORDER;
		}
	};

	private final String sorter;
	private String direction;

	private MenuSorter(final String sorter) {
		this.sorter = sorter;
	}

	abstract Ordering<CMGroup> getOrderingForProperty();

	public MenuSorter withDirection(final String direction) {
		this.direction = direction;
		return this;
	}

	Ordering<CMGroup> getOrientedOrdering() {
		direction = defaultIfBlank(direction, "ASC");
		if (direction.equalsIgnoreCase("DESC")) {
			return getOrderingForProperty().reverse();
		} else {
			return getOrderingForProperty();
		}
	}

	static MenuSorter of(final String field) {
		for (final MenuSorter element : values()) {
			if (element.sorter.equalsIgnoreCase(field)) {
				return element;
			}
		}
		return DEFAULT;
	}

	private static final Ordering<CMGroup> ORDER_BY_GROUP_NAME = new Ordering<CMGroup>() {
		@Override
		public int compare(final CMGroup left, final CMGroup right) {
			return left.getName().compareTo(right.getName());
		}
	};

	private static final Ordering<CMGroup> ORDER_BY_GROUP_DESCRIPTION = new Ordering<CMGroup>() {
		@Override
		public int compare(final CMGroup left, final CMGroup right) {
			return left.getDescription().compareTo(right.getDescription());
		}
	};

	private static final Ordering<CMGroup> DEFAULT_ORDER = ORDER_BY_GROUP_DESCRIPTION;
}
