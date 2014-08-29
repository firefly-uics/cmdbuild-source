package org.cmdbuild.service.rest.dto;

import static java.lang.Boolean.FALSE;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.service.rest.constants.Serialization.ATTRIBUTE;
import static org.cmdbuild.service.rest.constants.Serialization.ATTRIBUTES;
import static org.cmdbuild.service.rest.constants.Serialization.DESCRIPTION;
import static org.cmdbuild.service.rest.constants.Serialization.ID;
import static org.cmdbuild.service.rest.constants.Serialization.INSTRUCTIONS;
import static org.cmdbuild.service.rest.constants.Serialization.PROCESS_ACTIVITY;

import java.util.Collection;
import java.util.Collections;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.google.common.collect.Lists;

@XmlRootElement(name = PROCESS_ACTIVITY)
public class ProcessActivity {

	@XmlRootElement(name = ATTRIBUTE)
	public static class Attribute {

		public static class Builder implements org.apache.commons.lang3.builder.Builder<Attribute> {

			private String id;
			private Boolean writable;
			private Boolean mandatory;

			private Builder() {
				// use factory method
			}

			@Override
			public Attribute build() {
				validate();
				return new Attribute(this);
			}

			private void validate() {
				writable = defaultIfNull(writable, FALSE);
				mandatory = defaultIfNull(mandatory, FALSE);
			}

			public Builder withId(final String id) {
				this.id = id;
				return this;
			}

			public Builder withWritable(final Boolean writable) {
				this.writable = writable;
				return this;
			}

			public Builder withMandatory(final Boolean mandatory) {
				this.mandatory = mandatory;
				return this;
			}
		}

		public static Attribute.Builder newInstance() {
			return new Builder();
		}

		private String id;
		private boolean writable;
		private boolean mandatory;

		Attribute() {
			// package visibility
		}

		private Attribute(final Builder builder) {
			this.id = builder.id;
			this.writable = builder.writable;
			this.mandatory = builder.mandatory;
		}

		@XmlAttribute(name = ID)
		public String getId() {
			return id;
		}

		void setId(final String id) {
			this.id = id;
		}

		public boolean isWritable() {
			return writable;
		}

		void setWritable(final boolean writable) {
			this.writable = writable;
		}

		public boolean isMandatory() {
			return mandatory;
		}

		void setMandatory(final boolean mandatory) {
			this.mandatory = mandatory;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof Attribute)) {
				return false;
			}
			final Attribute other = Attribute.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.id, other.id) //
					.append(this.writable, other.writable) //
					.append(this.mandatory, other.mandatory) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(this.id) //
					.append(this.writable) //
					.append(this.mandatory) // //
					.toHashCode();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
		}

	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ProcessActivity> {

		private static final Collection<? extends Attribute> NO_ATTRIBUTES = Collections.emptyList();

		private String id;
		private String description;
		private String instructions;
		private final Collection<Attribute> attributes = Lists.newArrayList();

		private Builder() {
			// use static method
		}

		@Override
		public ProcessActivity build() {
			validate();
			return new ProcessActivity(this);
		}

		private void validate() {
			// TODO Auto-generated method stub
		}

		public Builder withId(final String id) {
			this.id = id;
			return this;
		}

		public Builder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public Builder withInstructions(final String instructions) {
			this.instructions = instructions;
			return this;
		}

		public Builder withAttributes(final Collection<? extends Attribute> attributes) {
			this.attributes.addAll(defaultIfNull(attributes, NO_ATTRIBUTES));
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	ProcessActivity() {
		// package visibility
	}

	private String id;
	private String description;
	private String instructions;
	private Collection<Attribute> attributes;

	private ProcessActivity(final Builder builder) {
		this.id = builder.id;
		this.description = builder.description;
		this.instructions = builder.instructions;
		this.attributes = builder.attributes;
	}

	@XmlAttribute(name = ID)
	public String getId() {
		return id;
	}

	void setId(final String id) {
		this.id = id;
	}

	@XmlAttribute(name = DESCRIPTION)
	public String getDescription() {
		return description;
	}

	void setDescription(final String description) {
		this.description = description;
	}

	@XmlAttribute(name = INSTRUCTIONS)
	public String getInstructions() {
		return instructions;
	}

	void setInstructions(final String instructions) {
		this.instructions = instructions;
	}

	@XmlAttribute(name = ATTRIBUTES)
	public Collection<Attribute> getAttributes() {
		return attributes;
	}

	void setAttributes(final Collection<Attribute> attributes) {
		this.attributes = attributes;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ProcessActivity)) {
			return false;
		}

		final ProcessActivity other = ProcessActivity.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.id, other.id) //
				.append(this.description, other.description) //
				.append(this.instructions, other.instructions) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(id) //
				.append(description) //
				.append(instructions) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
