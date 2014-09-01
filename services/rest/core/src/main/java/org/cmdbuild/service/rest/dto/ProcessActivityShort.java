package org.cmdbuild.service.rest.dto;

import static java.lang.Boolean.FALSE;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.service.rest.constants.Serialization.ATTRIBUTE;
import static org.cmdbuild.service.rest.constants.Serialization.ID;
import static org.cmdbuild.service.rest.constants.Serialization.PROCESS_ACTIVITY;
import static org.cmdbuild.service.rest.constants.Serialization.WRITABLE;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = PROCESS_ACTIVITY)
public class ProcessActivityShort {

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

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ProcessActivityShort> {

		private String id;
		private Boolean writable;

		private Builder() {
			// use static method
		}

		@Override
		public ProcessActivityShort build() {
			validate();
			return new ProcessActivityShort(this);
		}

		private void validate() {
			writable = defaultIfNull(writable, FALSE);
		}

		public Builder withId(final String id) {
			this.id = id;
			return this;
		}

		public Builder withWritableStatus(final boolean writable) {
			this.writable = writable;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	ProcessActivityShort() {
		// package visibility
	}

	private String id;
	private boolean writable;

	private ProcessActivityShort(final Builder builder) {
		this.id = builder.id;
		this.writable = builder.writable;
	}

	@XmlAttribute(name = ID)
	public String getId() {
		return id;
	}

	void setId(final String id) {
		this.id = id;
	}

	@XmlAttribute(name = WRITABLE)
	public boolean isWritable() {
		return writable;
	}

	public void setWritable(final boolean writable) {
		this.writable = writable;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ProcessActivityShort)) {
			return false;
		}

		final ProcessActivityShort other = ProcessActivityShort.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.id, other.id) //
				.append(this.writable, other.writable) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(id) //
				.append(writable) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
