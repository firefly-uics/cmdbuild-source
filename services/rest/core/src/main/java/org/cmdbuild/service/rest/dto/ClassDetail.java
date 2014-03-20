package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.dto.Constants.CLASS_DETAIL;
import static org.cmdbuild.service.rest.dto.Constants.DESCRIPTION;
import static org.cmdbuild.service.rest.dto.Constants.NAME;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@XmlRootElement(name = CLASS_DETAIL)
public class ClassDetail {

	public static class Builder implements org.cmdbuild.common.Builder<ClassDetail> {

		private String name;
		private String description;

		private Builder() {
			// use static method
		}

		@Override
		public ClassDetail build() {
			return new ClassDetail(this);
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

	ClassDetail() {
		// package visibility
	}

	private ClassDetail(final Builder builder) {
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

		if (!(obj instanceof ClassDetail)) {
			return false;
		}

		final ClassDetail other = ClassDetail.class.cast(obj);
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
