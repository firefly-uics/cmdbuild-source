package org.cmdbuild.service.rest.model;

import static org.cmdbuild.service.rest.constants.Serialization.DESCRIPTION;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_ID;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement
public class ProcessStatus extends AbstractModel {

	private Long id;
	private String description;

	ProcessStatus() {
		// package visibility
	}

	@XmlAttribute(name = UNDERSCORED_ID)
	@JsonProperty(UNDERSCORED_ID)
	public Long getId() {
		return id;
	}

	void setId(final Long id) {
		this.id = id;
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

		if (!(obj instanceof ProcessStatus)) {
			return false;
		}

		final ProcessStatus other = ProcessStatus.class.cast(obj);

		return new EqualsBuilder() //
				.append(this.getId(), other.getId()) //
				.append(this.description, other.description) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(this.getId()) //
				.append(this.description) //
				.toHashCode();
	}

}
