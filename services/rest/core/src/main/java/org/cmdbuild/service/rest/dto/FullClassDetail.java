package org.cmdbuild.service.rest.dto;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.service.rest.dto.Constants.DESCRIPTION;
import static org.cmdbuild.service.rest.dto.Constants.DESCRIPTION_ATTRIBUTE_NAME;
import static org.cmdbuild.service.rest.dto.Constants.FULL_CLASS_DETAIL;
import static org.cmdbuild.service.rest.dto.Constants.NAME;
import static org.cmdbuild.service.rest.dto.Constants.PARENT;
import static org.cmdbuild.service.rest.dto.Constants.SUPERCLASS;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement(name = FULL_CLASS_DETAIL)
public class FullClassDetail {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<FullClassDetail> {

		private String name;
		private String description;
		private Boolean superclass;
		private String descriptionAttributeName;
		private String parent;

		private Builder() {
			// use static method
		}

		@Override
		public FullClassDetail build() {
			validate();
			return new FullClassDetail(this);
		}

		private void validate() {
			superclass = defaultIfNull(superclass, false);
		}

		public Builder withName(final String name) {
			this.name = name;
			return this;
		}

		public Builder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public Builder withSuperclassStatus(final boolean superclass) {
			this.superclass = superclass;
			return this;
		}

		public Builder withDescriptionAttributeName(final String descriptionAttributeName) {
			this.descriptionAttributeName = descriptionAttributeName;
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
	private boolean superclass;
	private String descriptionAttributeName;
	private String parent;

	FullClassDetail() {
		// package visibility
	}

	private FullClassDetail(final Builder builder) {
		this.name = builder.name;
		this.description = builder.description;
		this.superclass = builder.superclass;
		this.descriptionAttributeName = builder.descriptionAttributeName;
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

	@XmlAttribute(name = SUPERCLASS)
	public boolean isSuperclass() {
		return superclass;
	}

	void setSuperclass(final boolean superclass) {
		this.superclass = superclass;
	}

	@XmlAttribute(name = DESCRIPTION_ATTRIBUTE_NAME)
	@JsonProperty(DESCRIPTION_ATTRIBUTE_NAME)
	public String getDescriptionAttributeName() {
		return descriptionAttributeName;
	}

	void setDescriptionAttributeName(final String descriptionAttributeName) {
		this.descriptionAttributeName = descriptionAttributeName;
	}

	@XmlAttribute(name = PARENT)
	public String getParent() {
		return parent;
	}

	void setParent(final String parent) {
		this.parent = parent;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof FullClassDetail)) {
			return false;
		}

		final FullClassDetail other = FullClassDetail.class.cast(obj);
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
