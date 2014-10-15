package org.cmdbuild.service.rest.model;

import static org.cmdbuild.service.rest.constants.Serialization.DESCRIPTION;
import static org.cmdbuild.service.rest.constants.Serialization.NAME;
import static org.cmdbuild.service.rest.constants.Serialization.SIMPLE_DOMAIN_DETAIL;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement(name = SIMPLE_DOMAIN_DETAIL)
public class DomainWithBasicDetails extends ModelWithId<String> {

	private String name;
	private String description;

	DomainWithBasicDetails() {
		// package visibility
	}

	@XmlAttribute(name = NAME)
	public String getName() {
		return name;
	}

	void setName(final String name) {
		this.name = name;
	}

	@XmlAttribute(name = DESCRIPTION)
	public String getDescription() {
		return description;
	}

	void setDescription(final String description) {
		this.description = description;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof DomainWithBasicDetails)) {
			return false;
		}

		final DomainWithBasicDetails other = DomainWithBasicDetails.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.name, other.name) //
				.append(this.description, other.description) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(name) //
				.append(description) //
				.toHashCode();
	}

}
