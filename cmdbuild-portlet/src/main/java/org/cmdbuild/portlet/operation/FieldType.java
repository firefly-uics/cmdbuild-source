package org.cmdbuild.portlet.operation;

import org.cmdbuild.services.soap.AttributeSchema;

public enum FieldType {

	STRING(false) {

		@Override
		public int getHeaderLength(final AttributeSchema as) {
			int weight = (int) Math.round(as.getLength() * 1.5);
			if (weight < 20) {
				weight = 20;
			} else if (weight > 70) {
				weight = 60;
			}
			return weight;
		}
	},
	TEXT(false) {

		@Override
		public int getHeaderLength(final AttributeSchema as) {
			return 50;
		}
	},
	DATE(false) {

		@Override
		public int getHeaderLength(final AttributeSchema as) {
			return 30;
		}
	},
	TIMESTAMP(false) {

		@Override
		public int getHeaderLength(final AttributeSchema as) {
			return 50;
		}
	},
	NUMBER(false) {

		@Override
		public int getHeaderLength(final AttributeSchema as) {
			return 50;
		}
	},
	INTEGER(false) {

		@Override
		public int getHeaderLength(final AttributeSchema as) {
			return 20;
		}
	},
	DECIMAL(false) {

		@Override
		public int getHeaderLength(final AttributeSchema as) {
			return 20;
		}
	},
	DOUBLE(false) {

		@Override
		public int getHeaderLength(final AttributeSchema as) {
			return 20;
		}
	},
	LOOKUP(false) {

		@Override
		public int getHeaderLength(final AttributeSchema as) {
			return 50;
		}
	},
	REFERENCE(false) {

		@Override
		public int getHeaderLength(final AttributeSchema as) {
			return 50;
		}
	},
	BOOLEAN(true);
	private boolean fixed;

	FieldType(final boolean fixed) {
		this.fixed = fixed;
	}

	public int getHeaderLength(final AttributeSchema as) {
		return 40;
	}

	public boolean isFixed() {
		return fixed;
	}
}
