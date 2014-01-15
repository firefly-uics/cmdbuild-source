package org.cmdbuild.service.rest.serialization.data;

import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entry.LookupValue;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.service.rest.dto.data.AttributeDetail;

import com.google.common.base.Function;

public class ToAttributeDetail implements Function<Entry<String, Object>, AttributeDetail> {

	public static class Builder implements org.cmdbuild.common.Builder<ToAttributeDetail> {

		private CMEntryType entryType;

		private Builder() {
			// use static method
		}

		@Override
		public ToAttributeDetail build() {
			validate();
			return new ToAttributeDetail(this);
		}

		private void validate() {
			Validate.notNull(entryType, "invalid entry type");
		}

		public Builder with(final CMEntryType entryType) {
			this.entryType = entryType;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final CMEntryType entryType;

	private ToAttributeDetail(final Builder builder) {
		this.entryType = builder.entryType;
	}

	@Override
	public AttributeDetail apply(final Entry<String, Object> input) {
		return AttributeDetail.newInstance() //
				.withName(input.getKey()) //
				.withValue(convert(input.getKey(), input.getValue())) //
				.build();
	}

	private String convert(final String name, final Object value) {
		return new NullAttributeTypeVisitor() {

			private String converted = null;

			public String convert() {
				entryType.getAttribute(name).getType().accept(this);
				return (converted == null) ? String.class.cast(value) : converted;
			}

			@Override
			public void visit(final ForeignKeyAttributeType attributeType) {
				final IdAndDescription idAndDescription = IdAndDescription.class.cast(value);
				converted = idAndDescription.getId().toString();
			}

			@Override
			public void visit(final LookupAttributeType attributeType) {
				final LookupValue lookupValue = LookupValue.class.cast(value);
				converted = lookupValue.getId().toString();
			}

			@Override
			public void visit(final ReferenceAttributeType attributeType) {
				final IdAndDescription idAndDescription = IdAndDescription.class.cast(value);
				converted = idAndDescription.getId().toString();
			};

		}.convert();
	}

}
