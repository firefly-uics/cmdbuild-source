package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.dto.Constants.DESCRIPTION;
import static org.cmdbuild.service.rest.dto.Constants.NAME;
import static org.cmdbuild.service.rest.dto.Constants.PARENT;
import static org.cmdbuild.service.rest.dto.Constants.SIMPLE_CLASS_DETAIL;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = SIMPLE_CLASS_DETAIL)
public class SimpleClassDetail {

	public static class Builder implements org.cmdbuild.common.Builder<SimpleClassDetail> {

		private String name;
		private String description;
		private String parent;

		private Builder() {
			// use static method
		}

		@Override
		public SimpleClassDetail build() {
			return new SimpleClassDetail(this);
		}

		public Builder withName(final String name) {
			this.name = name;
			return this;
		}

		public Builder withDescription(final String description) {
			this.description = description;
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
	private String description;
	private String parent;

	SimpleClassDetail() {
		// package visibility
	}

	private SimpleClassDetail(final Builder builder) {
		this.name = builder.name;
		this.description = builder.description;
		this.parent = builder.parent;
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

	@XmlAttribute(name = PARENT)
	public String getParent() {
		return parent;
	}

	void setParent(String parent) {
		this.parent = parent;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof SimpleClassDetail)) {
			return false;
		}

		final SimpleClassDetail other = SimpleClassDetail.class.cast(obj);
		return name.equals(other.name);
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
