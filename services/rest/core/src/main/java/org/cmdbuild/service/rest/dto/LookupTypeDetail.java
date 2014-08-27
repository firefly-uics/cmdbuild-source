package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.constants.Serialization.LOOKUP_TYPE_DETAIL;
import static org.cmdbuild.service.rest.constants.Serialization.NAME;
import static org.cmdbuild.service.rest.constants.Serialization.PARENT;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = LOOKUP_TYPE_DETAIL)
public class LookupTypeDetail {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<LookupTypeDetail> {

		private String name;
		private String parent;

		private Builder() {
			// use static method
		}

		@Override
		public LookupTypeDetail build() {
			return new LookupTypeDetail(this);
		}

		public Builder withName(final String name) {
			this.name = name;
			return this;
		}

		public Builder withParent(final String parent) {
			this.parent = parent;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private String name;
	private String parent;

	LookupTypeDetail() {
		// package visibility
	}

	private LookupTypeDetail(final Builder builder) {
		this.name = builder.name;
		this.parent = builder.parent;
	}

	@XmlAttribute(name = NAME)
	public String getName() {
		return name;
	}

	void setName(final String name) {
		this.name = name;
	}

	@XmlAttribute(name = PARENT)
	public String getParent() {
		return parent;
	}

	void setDescription(final String description) {
		this.parent = description;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof LookupTypeDetail)) {
			return false;
		}

		final LookupTypeDetail other = LookupTypeDetail.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.name, other.name) //
				.append(this.parent, other.parent) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(name) //
				.append(parent) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
