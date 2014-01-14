package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.dto.Constants.ACTIVE;
import static org.cmdbuild.service.rest.dto.Constants.ATTRIBUTE_DETAIL;
import static org.cmdbuild.service.rest.dto.Constants.DEFAULT_VALUE;
import static org.cmdbuild.service.rest.dto.Constants.DESCRIPTION;
import static org.cmdbuild.service.rest.dto.Constants.DISPLAYABLE_IN_LIST;
import static org.cmdbuild.service.rest.dto.Constants.GROUP;
import static org.cmdbuild.service.rest.dto.Constants.INDEX;
import static org.cmdbuild.service.rest.dto.Constants.INHERITED;
import static org.cmdbuild.service.rest.dto.Constants.MANDATORY;
import static org.cmdbuild.service.rest.dto.Constants.NAME;
import static org.cmdbuild.service.rest.dto.Constants.NAMESPACE;
import static org.cmdbuild.service.rest.dto.Constants.UNIQUE;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@XmlRootElement(name = ATTRIBUTE_DETAIL, namespace = NAMESPACE)
public class AttributeDetail {

	public static class Builder implements org.cmdbuild.common.Builder<AttributeDetail> {

		private String name;
		private String description;
		private boolean displayableInList;
		private boolean unique;
		private boolean mandatory;
		private boolean inherited;
		private boolean active;
		private int index;
		private String defaultValue;
		private String group;

		private Builder() {
			// use static method
		}

		@Override
		public AttributeDetail build() {
			return new AttributeDetail(this);
		}

		public Builder withName(final String name) {
			this.name = name;
			return this;
		}

		public Builder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public Builder thatIsDisplayableInList(final boolean displayableInList) {
			this.displayableInList = displayableInList;
			return this;
		}

		public Builder thatIsUnique(final boolean unique) {
			this.unique = unique;
			return this;
		}

		public Builder thatIsMandatory(final boolean mandatory) {
			this.mandatory = mandatory;
			return this;
		}

		public Builder thatIsInherited(final boolean inherited) {
			this.inherited = inherited;
			return this;
		}

		public Builder thatIsActive(final boolean active) {
			this.active = active;
			return this;
		}

		public Builder withIndex(final int index) {
			this.index = index;
			return this;
		}

		public Builder withDefaultValue(final String defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}

		public Builder withGroup(final String group) {
			this.group = group;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private String name;
	private String description;
	private boolean displayableInList;
	private boolean unique;
	private boolean mandatory;
	private boolean inherited;
	private boolean active;
	private int index;
	private String defaultValue;
	private String group;

	AttributeDetail() {
		// package visibility
	}

	private AttributeDetail(final Builder builder) {
		this.name = builder.name;
		this.description = builder.description;
		this.displayableInList = builder.displayableInList;
		this.unique = builder.unique;
		this.mandatory = builder.mandatory;
		this.inherited = builder.inherited;
		this.active = builder.active;
		this.index = builder.index;
		this.defaultValue = builder.defaultValue;
		this.group = builder.group;
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

	@XmlAttribute(name = DISPLAYABLE_IN_LIST)
	public boolean isDisplayableInList() {
		return displayableInList;
	}

	void setDisplayableInList(final boolean displayableInList) {
		this.displayableInList = displayableInList;
	}

	@XmlAttribute(name = UNIQUE)
	public boolean isUnique() {
		return unique;
	}

	void setUnique(final boolean unique) {
		this.unique = unique;
	}

	@XmlAttribute(name = MANDATORY)
	public boolean isMandatory() {
		return mandatory;
	}

	void setMandatory(final boolean mandatory) {
		this.mandatory = mandatory;
	}

	@XmlAttribute(name = INHERITED)
	public boolean isInherited() {
		return inherited;
	}

	void setInherited(final boolean inherited) {
		this.inherited = inherited;
	}

	@XmlAttribute(name = ACTIVE)
	public boolean isActive() {
		return active;
	}

	void setActive(final boolean active) {
		this.active = active;
	}

	@XmlAttribute(name = INDEX)
	public int getIndex() {
		return index;
	}

	void setIndex(final int index) {
		this.index = index;
	}

	@XmlAttribute(name = DEFAULT_VALUE)
	public String getDefaultValue() {
		return defaultValue;
	}

	void setDefaultValue(final String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@XmlAttribute(name = GROUP)
	public String getGroup() {
		return group;
	}

	void setGroup(final String group) {
		this.group = group;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof AttributeDetail)) {
			return false;
		}

		final AttributeDetail other = AttributeDetail.class.cast(obj);
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
