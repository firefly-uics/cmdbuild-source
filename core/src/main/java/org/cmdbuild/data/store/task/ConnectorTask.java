package org.cmdbuild.data.store.task;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ConnectorTask extends Task {

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

	public static Builder<ConnectorTask> newInstance() {
		return new Builder<ConnectorTask>() {

			@Override
			protected ConnectorTask doBuild() {
				return new ConnectorTask(this);
			}

		};
	}

	private ConnectorTask(final Builder<? extends Task> builder) {
		super(builder);
	}

	@Override
	public void accept(final TaskVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected Builder<? extends Task> builder() {
		return newInstance();
	}

}
