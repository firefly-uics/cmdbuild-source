package org.cmdbuild.services.sync.store.internal;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class BuildableAttributeMapping implements AttributeMapping {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<BuildableAttributeMapping> {

		private String from;
		private String to;

		private Builder() {
			// use factory method
		}

		@Override
		public BuildableAttributeMapping build() {
			validate();
			return new BuildableAttributeMapping(this);
		}

		private void validate() {
			Validate.notBlank(from, "invalid from '%s'", from);
			Validate.notBlank(to, "invalid to '%s'", to);
		}

		public Builder withFrom(final String from) {
			this.from = from;
			return this;
		}

		public Builder withTo(final String to) {
			this.to = to;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final String from;
	private final String to;
	private final int hashCode;

	private BuildableAttributeMapping(final Builder builder) {
		this.from = builder.from;
		this.to = builder.to;
		this.hashCode = new HashCodeBuilder() //
				.append(this.from) //
				.append(this.to) //
				.toHashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof BuildableAttributeMapping)) {
			return false;
		}
		final BuildableAttributeMapping other = BuildableAttributeMapping.class.cast(obj);
		return from.equals(other.from) && to.equals(other.to);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public String from() {
		return from;
	}

	@Override
	public String to() {
		return to;
	}

}
