package org.cmdbuild.service.rest.dto.schema;

import static org.cmdbuild.service.rest.dto.Constants.LOOKUP_TYPE_DETAIL;
import static org.cmdbuild.service.rest.dto.Constants.NAME;
import static org.cmdbuild.service.rest.dto.Constants.NAMESPACE;
import static org.cmdbuild.service.rest.dto.Constants.PARENT;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@XmlRootElement(name = LOOKUP_TYPE_DETAIL, namespace = NAMESPACE)
public class LookupTypeDetail {

	public static class Builder implements org.cmdbuild.common.Builder<LookupTypeDetail> {

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
		return name.equals(other.name) && parent.equals(other.parent);
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
