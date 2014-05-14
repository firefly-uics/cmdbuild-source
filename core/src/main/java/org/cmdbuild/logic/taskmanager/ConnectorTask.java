package org.cmdbuild.logic.taskmanager;

import static com.google.common.collect.Iterables.addAll;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Collection;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.google.common.collect.Sets;

public class ConnectorTask implements ScheduledTask {
	
	public static class AttributeMapping {

		public static class Builder implements org.apache.commons.lang3.builder.Builder<AttributeMapping> {

			private String sourceType;
			private String sourceAttribute;
			private String targetType;
			private String targetAttribute;
			private Boolean isKey;

			private Builder() {
				// user factory method
			}

			@Override
			public AttributeMapping build() {
				validate();
				return new AttributeMapping(this);
			}

			private void validate() {
				isKey = defaultIfNull(isKey, false);
			}

			public Builder withSourceType(final String sourceType) {
				this.sourceType = sourceType;
				return this;
			}

			public Builder withSourceAttribute(final String sourceAttribute) {
				this.sourceAttribute = sourceAttribute;
				return this;
			}

			public Builder withTargetType(final String targetType) {
				this.targetType = targetType;
				return this;
			}

			public Builder withTargetAttribute(final String targetAttribute) {
				this.targetAttribute = targetAttribute;
				return this;
			}

			public Builder withKeyStatus(final boolean isKey) {
				this.isKey = isKey;
				return this;
			}

		}

		public static Builder newInstance() {
			return new Builder();
		}

		private final String sourceType;
		private final String sourceAttribute;
		private final String targetType;
		private final String targetAttribute;
		private final boolean isKey;

		private AttributeMapping(final Builder builder) {
			this.sourceType = builder.sourceType;
			this.sourceAttribute = builder.sourceAttribute;
			this.targetType = builder.targetType;
			this.targetAttribute = builder.targetAttribute;
			this.isKey = builder.isKey;
		}

		public String getSourceType() {
			return sourceType;
		}

		public String getSourceAttribute() {
			return sourceAttribute;
		}

		public String getTargetType() {
			return targetType;
		}

		public String getTargetAttribute() {
			return targetAttribute;
		}

		public boolean isKey() {
			return isKey;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof AttributeMapping)) {
				return false;
			}
			final AttributeMapping other = AttributeMapping.class.cast(obj);
			return new EqualsBuilder() //
					.append(sourceType, other.sourceType) //
					.append(sourceAttribute, other.sourceAttribute) //
					.append(targetType, other.targetType) //
					.append(targetAttribute, other.targetAttribute) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(sourceType) //
					.append(sourceAttribute) //
					.append(targetType) //
					.append(targetAttribute) //
					.toHashCode();
		}

	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ConnectorTask> {

		private Long id;
		private String description;
		private Boolean active;
		private String cronExpression;
		private final Collection<AttributeMapping> attributeMappings = Sets.newHashSet();

		private Builder() {
			// use factory method
		}

		@Override
		public ConnectorTask build() {
			validate();
			return new ConnectorTask(this);
		}

		private void validate() {
			active = (active == null) ? false : active;
		}

		public Builder withId(final Long id) {
			this.id = id;
			return this;
		}

		public Builder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public Builder withActiveStatus(final boolean active) {
			this.active = active;
			return this;
		}

		public Builder withCronExpression(final String cronExpression) {
			this.cronExpression = cronExpression;
			return this;
		}

		public Builder withAttributeMapping(final Iterable<? extends AttributeMapping> attributeMappings) {
			addAll(this.attributeMappings, attributeMappings);
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Long id;
	private final String description;
	private final boolean active;
	private final String cronExpression;
	private final Iterable<AttributeMapping> attributeMappings;

	private ConnectorTask(final Builder builder) {
		this.id = builder.id;
		this.description = builder.description;
		this.active = builder.active;
		this.cronExpression = builder.cronExpression;
		this.attributeMappings = builder.attributeMappings;
	}

	@Override
	public void accept(final TaskVistor visitor) {
		visitor.visit(this);
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public String getCronExpression() {
		return cronExpression;
	}

	public Iterable<AttributeMapping> getAttributeMappings() {
		return attributeMappings;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
