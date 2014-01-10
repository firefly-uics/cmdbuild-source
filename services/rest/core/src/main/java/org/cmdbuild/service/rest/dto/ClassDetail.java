package org.cmdbuild.service.rest.dto;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@XmlRootElement(name = "class")
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

	private final String name;
	private final String description;

	public ClassDetail(final Builder builder) {
		this.name = builder.name;
		this.description = builder.description;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
