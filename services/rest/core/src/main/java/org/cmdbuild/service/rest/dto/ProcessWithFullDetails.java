package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.constants.Serialization.DESCRIPTION_ATTRIBUTE_NAME;
import static org.cmdbuild.service.rest.constants.Serialization.FULL_CLASS_DETAIL;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement(name = FULL_CLASS_DETAIL)
public class ProcessWithFullDetails extends ProcessWithBasicDetails {

	private String descriptionAttributeName;

	ProcessWithFullDetails() {
		// package visibility
	}

	@XmlAttribute(name = DESCRIPTION_ATTRIBUTE_NAME)
	@JsonProperty(DESCRIPTION_ATTRIBUTE_NAME)
	public String getDescriptionAttributeName() {
		return descriptionAttributeName;
	}

	void setDescriptionAttributeName(final String descriptionAttributeName) {
		this.descriptionAttributeName = descriptionAttributeName;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof ProcessWithFullDetails)) {
			return false;
		}

		final ProcessWithFullDetails other = ProcessWithFullDetails.class.cast(obj);
		return super.doEquals(other) && new EqualsBuilder() //
				.append(this.descriptionAttributeName, other.descriptionAttributeName) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(super.doHashCode()) //
				.append(this.descriptionAttributeName) //
				.toHashCode();
	}

}
