package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.dto.Constants.ATTRIBUTE_DETAIL;
import static org.cmdbuild.service.rest.dto.Constants.NAME;
import static org.cmdbuild.service.rest.dto.Constants.VALUE;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@XmlRootElement(name = ATTRIBUTE_DETAIL)
public class AttributeValueDetail {

	public static class Builder implements org.cmdbuild.common.Builder<AttributeValueDetail> {

		private String name;
		private String value;

		private Builder() {
			// use static method
		}

		@Override
		public AttributeValueDetail build() {
			validate();
			return new AttributeValueDetail(this);
		}

		private void validate() {
			Validate.isTrue(StringUtils.isNotBlank(name), "invalid name");

		}

		public Builder withName(final String name) {
			this.name = name;
			return this;
		}

		public Builder withValue(final String value) {
			this.value = value;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private String name;
	private String value;

	AttributeValueDetail() {
		// package visibility
	}

	private AttributeValueDetail(final Builder builder) {
		this.name = builder.name;
		this.value = builder.value;
	}

	@XmlAttribute(name = NAME)
	public String getName() {
		return name;
	}

	void setName(final String name) {
		this.name = name;
	}

	@XmlAttribute(name = VALUE)
	public String getValue() {
		return value;
	}

	void setValue(final String value) {
		this.value = value;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof AttributeValueDetail)) {
			return false;
		}

		final AttributeValueDetail other = AttributeValueDetail.class.cast(obj);
		return name.equals(other.name) && value.equals(other.value);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
