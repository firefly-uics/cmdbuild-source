package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.constants.Serialization.DESCRIPTION;
import static org.cmdbuild.service.rest.constants.Serialization.NAME;
import static org.cmdbuild.service.rest.constants.Serialization.SIMPLE_DOMAIN_DETAIL;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = SIMPLE_DOMAIN_DETAIL)
public class SimpleDomainDetail {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<SimpleDomainDetail> {

		private String name;
		private String description;

		private Builder() {
			// use static method
		}

		@Override
		public SimpleDomainDetail build() {
			validate();
			return new SimpleDomainDetail(this);
		}

		private void validate() {
			// TODO Auto-generated method stub
		}

		public Builder withName(final String name) {
			this.name = name;
			return this;
		}

		public Builder withDescription(final String description) {
			this.description = description;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private String name;
	private String description;

	SimpleDomainDetail() {
		// package visibility
	}

	private SimpleDomainDetail(final Builder builder) {
		this.name = builder.name;
		this.description = builder.description;
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
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof SimpleDomainDetail)) {
			return false;
		}

		final SimpleDomainDetail other = SimpleDomainDetail.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.name, other.name) //
				.append(this.description, other.description) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(name) //
				.append(description) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
