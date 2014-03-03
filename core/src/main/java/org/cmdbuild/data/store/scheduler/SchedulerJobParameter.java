package org.cmdbuild.data.store.scheduler;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import static org.apache.commons.lang.builder.ToStringStyle.*;

import org.cmdbuild.data.store.Storable;

public class SchedulerJobParameter implements Storable {

	public static class Builder implements org.cmdbuild.common.Builder<SchedulerJobParameter> {

		private Long id;
		private Long owner;
		private String key;
		private String value;

		private Builder() {
			// use factory method
		}

		@Override
		public SchedulerJobParameter build() {
			validate();
			return new SchedulerJobParameter(this);
		}

		private void validate() {
			Validate.notNull(owner, "invalid owner");
			Validate.isTrue(isNotBlank(key), "invalid key");
		}

		public Builder withId(final Long id) {
			this.id = id;
			return this;
		}

		public Builder withOwner(final Long owner) {
			this.owner = owner;
			return this;
		}

		public Builder withKey(final String key) {
			this.key = key;
			return this;
		}

		public Builder withValue(final String value) {
			this.value = value;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Long id;
	private final Long owner;
	private final String key;
	private final String value;

	public SchedulerJobParameter() {
		this(null);
	}

	public SchedulerJobParameter(final Builder builder) {
		this.id = builder.id;
		this.owner = builder.owner;
		this.key = builder.key;
		this.value = builder.value;
	}

	@Override
	public String getIdentifier() {
		return id.toString();
	}

	public Long getId() {
		return id;
	}

	public Long getOwner() {
		return owner;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}
	
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof SchedulerJobParameter)) {
			return false;
		}
		final SchedulerJobParameter other = SchedulerJobParameter.class.cast(obj);
		return EqualsBuilder.reflectionEquals(this, other);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE);
	}

}
