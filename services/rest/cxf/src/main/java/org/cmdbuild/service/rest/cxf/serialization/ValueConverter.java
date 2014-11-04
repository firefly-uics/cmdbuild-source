package org.cmdbuild.service.rest.cxf.serialization;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;

class ValueConverter {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ValueConverter> {

		private CMClass type;

		private Builder() {
			// use static method
		}

		@Override
		public ValueConverter build() {
			validate();
			return new ValueConverter(this);
		}

		private void validate() {
			Validate.notNull(type, "missing '%s'", CMClass.class);
		}

		public Builder withType(final CMClass type) {
			this.type = type;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private static final IdAndDescription NULL = new IdAndDescription(null, null);

	private final CMClass type;

	private ValueConverter(final Builder builder) {
		this.type = builder.type;
	}

	public Object convert(final String name, final Object value) {
		return new NullAttributeTypeVisitor() {

			private Object converted;

			public Object value() {
				converted = value;
				type.getAttribute(name).getType() //
						.accept(this);
				return converted;
			}

			@Override
			public void visit(final ForeignKeyAttributeType attributeType) {
				converted = idFromIdAndDescription(value);
			}

			@Override
			public void visit(final LookupAttributeType attributeType) {
				converted = idFromIdAndDescription(value);
			};

			@Override
			public void visit(final ReferenceAttributeType attributeType) {
				converted = idFromIdAndDescription(value);
			}

			private Object idFromIdAndDescription(final Object value) {
				return ((value == null) ? NULL : IdAndDescription.class.cast(value)).getId();
			}

		}.value();
	}

}
