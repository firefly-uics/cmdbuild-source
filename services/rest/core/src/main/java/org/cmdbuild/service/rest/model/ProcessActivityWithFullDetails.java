package org.cmdbuild.service.rest.model;

import static org.cmdbuild.service.rest.constants.Serialization.ATTRIBUTE;
import static org.cmdbuild.service.rest.constants.Serialization.ATTRIBUTES;
import static org.cmdbuild.service.rest.constants.Serialization.DESCRIPTION;
import static org.cmdbuild.service.rest.constants.Serialization.INSTRUCTIONS;
import static org.cmdbuild.service.rest.constants.Serialization.PROCESS_ACTIVITY_DEFINITION;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement(name = PROCESS_ACTIVITY_DEFINITION)
public class ProcessActivityWithFullDetails extends ModelWithId<String> {

	@XmlRootElement(name = ATTRIBUTE)
	public static class AttributeStatus extends ModelWithId<String> {

		private boolean writable;
		private boolean mandatory;
		private Long index;

		AttributeStatus() {
			// package visibility
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

		public Long getIndex() {
			return index;
		}

		void setIndex(final Long index) {
			this.index = index;
		}

		@Override
		protected boolean doEquals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof AttributeStatus)) {
				return false;
			}
			final AttributeStatus other = AttributeStatus.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.getId(), other.getId()) //
					.append(this.writable, other.writable) //
					.append(this.mandatory, other.mandatory) //
					.append(this.index, other.index) //
					.isEquals();
		}

		@Override
		protected int doHashCode() {
			return new HashCodeBuilder() //
					.append(this.getId()) //
					.append(this.writable) //
					.append(this.mandatory) //
					.append(this.index) //
					.toHashCode();
		}

	}

	private String description;
	private String instructions;
	private Collection<AttributeStatus> attributes;

	ProcessActivityWithFullDetails() {
		// package visibility
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

	@XmlElement(name = ATTRIBUTES)
	public Collection<AttributeStatus> getAttributes() {
		return attributes;
	}

	void setAttributes(final Collection<AttributeStatus> attributes) {
		this.attributes = attributes;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ProcessActivityWithFullDetails)) {
			return false;
		}

		final ProcessActivityWithFullDetails other = ProcessActivityWithFullDetails.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.getId(), other.getId()) //
				.append(this.description, other.description) //
				.append(this.instructions, other.instructions) //
				.append(this.attributes, other.attributes) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(getId()) //
				.append(description) //
				.append(instructions) //
				.append(attributes) //
				.toHashCode();
	}

}
