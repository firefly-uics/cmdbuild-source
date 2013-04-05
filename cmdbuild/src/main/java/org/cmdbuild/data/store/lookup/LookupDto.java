package org.cmdbuild.data.store.lookup;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.cmdbuild.common.Builder;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.data.store.lookup.LookupTypeDto.LookupTypeDtoBuilder;

public final class LookupDto implements Storable {

	public static class LookupDtoBuilder implements Builder<LookupDto> {

		private Long id;
		private String code;
		private String description;
		private String notes;
		private LookupTypeDto type;
		private Integer number = 0;
		private boolean active;
		private boolean isDefault;
		private Long parentId;
		private LookupDto parent;

		/**
		 * instantiate using {@link LookupDto#newInstance()}
		 */
		private LookupDtoBuilder() {
		}

		public LookupDto.LookupDtoBuilder clone(final LookupDto lookup) {
			this.id = lookup.id;
			this.code = lookup.code;
			this.description = lookup.description;
			this.notes = lookup.notes;
			this.type = lookup.type;
			this.number = lookup.number;
			this.active = lookup.active;
			this.isDefault = lookup.isDefault;
			this.parentId = lookup.parentId;
			this.parent = lookup.parent;
			return this;
		}

		public LookupDto.LookupDtoBuilder withId(final Long value) {
			this.id = value;
			return this;
		}

		public LookupDto.LookupDtoBuilder withCode(final String value) {
			this.code = value;
			return this;
		}

		public LookupDto.LookupDtoBuilder withDescription(final String value) {
			this.description = value;
			return this;
		}

		public LookupDtoBuilder withNotes(final String value) {
			this.notes = value;
			return this;
		}

		public LookupDto.LookupDtoBuilder withType(final LookupTypeDtoBuilder builder) {
			return withType(builder.build());
		}

		public LookupDto.LookupDtoBuilder withType(final LookupTypeDto value) {
			this.type = value;
			return this;
		}

		public LookupDto.LookupDtoBuilder withNumber(final Integer value) {
			this.number = value;
			return this;
		}

		public LookupDto.LookupDtoBuilder withActiveStatus(final boolean value) {
			this.active = value;
			return this;
		}

		public LookupDto.LookupDtoBuilder withDefaultStatus(final boolean value) {
			this.isDefault = value;
			return this;
		}

		public LookupDto.LookupDtoBuilder withParentId(final Long value) {
			this.parentId = value;
			return this;
		}

		public LookupDto.LookupDtoBuilder withParent(final LookupDto value) {
			this.parentId = value.id;
			this.parent = value;
			return this;
		}

		@Override
		public LookupDto build() {
			return new LookupDto(this);
		}

	}

	public static LookupDto.LookupDtoBuilder newInstance() {
		return new LookupDtoBuilder();
	}

	public final Long id;
	public final String code;
	public final String description;
	public final String notes;
	public final LookupTypeDto type;
	public final Integer number;
	public final boolean active;
	public final boolean isDefault;
	public final Long parentId;
	public final LookupDto parent;

	private final transient String toString;

	private LookupDto(final LookupDtoBuilder builder) {
		this.id = builder.id;
		this.code = builder.code;
		this.description = builder.description;
		this.notes = builder.notes;
		this.type = builder.type;
		this.number = builder.number;
		this.active = builder.active;
		this.isDefault = builder.isDefault;
		this.parentId = builder.parentId;
		this.parent = builder.parent;

		this.toString = ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
	}

	@Override
	public String getIdentifier() {
		return id.toString();
	}

	@Override
	public String toString() {
		return toString;
	}

}
