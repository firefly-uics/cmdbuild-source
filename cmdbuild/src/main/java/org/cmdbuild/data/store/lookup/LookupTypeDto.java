package org.cmdbuild.data.store.lookup;

import static org.apache.commons.lang.StringUtils.defaultIfBlank;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.cmdbuild.common.Builder;

public final class LookupTypeDto {

	public static class LookupTypeDtoBuilder implements Builder<LookupTypeDto> {

		private String name;
		private String parent;

		/**
		 * instantiate using {@link LookupTypeDto#newInstance()}
		 */
		private LookupTypeDtoBuilder() {
		}

		public LookupTypeDto.LookupTypeDtoBuilder withName(final String value) {
			this.name = value;
			return this;
		}

		public LookupTypeDto.LookupTypeDtoBuilder withParent(final String value) {
			this.parent = value;
			return this;
		}

		@Override
		public LookupTypeDto build() {
			this.name = defaultIfBlank(name, null);
			this.parent = defaultIfBlank(parent, null);

			return new LookupTypeDto(this);
		}

	}

	public static LookupTypeDto.LookupTypeDtoBuilder newInstance() {
		return new LookupTypeDtoBuilder();
	}

	public final String name;
	public final String parent;

	private final transient int hashCode;
	private final transient String toString;

	public LookupTypeDto(final LookupTypeDtoBuilder builder) {
		this.name = builder.name;
		this.parent = builder.parent;

		this.hashCode = new HashCodeBuilder() //
				.append(this.name) //
				.append(this.parent) //
				.toHashCode();
		this.toString = new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE) //
				.append("name", name) //
				.append("parent", parent) //
				.toString();
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof LookupTypeDto)) {
			return false;
		}
		final LookupTypeDto other = LookupTypeDto.class.cast(obj);
		return new EqualsBuilder() //
				.append(name, other.name) //
				.append(parent, other.parent) //
				.isEquals();
	}

	@Override
	public String toString() {
		return toString;
	}

}
