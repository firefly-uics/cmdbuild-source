package org.cmdbuild.service.rest.model;

import static org.cmdbuild.service.rest.constants.Serialization.LOOKUP_TYPE_DETAIL;
import static org.cmdbuild.service.rest.constants.Serialization.NAME;
import static org.cmdbuild.service.rest.constants.Serialization.PARENT;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement(name = LOOKUP_TYPE_DETAIL)
public class LookupTypeDetail extends ModelWithId {

	private String name;
	private Long parent;

	LookupTypeDetail() {
		// package visibility
	}

	@XmlAttribute(name = NAME)
	public String getName() {
		return name;
	}

	void setName(final String name) {
		this.name = name;
	}

	@XmlAttribute(name = PARENT)
	public Long getParent() {
		return parent;
	}

	void setParent(final Long parent) {
		this.parent = parent;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof LookupTypeDetail)) {
			return false;
		}

		final LookupTypeDetail other = LookupTypeDetail.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.getId(), other.getId()) //
				.append(this.name, other.name) //
				.append(this.parent, other.parent) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(getId()) //
				.append(name) //
				.append(parent) //
				.toHashCode();
	}

}
