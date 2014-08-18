package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.constants.Serialization.ACTIVE;
import static org.cmdbuild.service.rest.constants.Serialization.ATTRIBUTE_DETAIL;
import static org.cmdbuild.service.rest.constants.Serialization.DEFAULT_VALUE;
import static org.cmdbuild.service.rest.constants.Serialization.DESCRIPTION;
import static org.cmdbuild.service.rest.constants.Serialization.DISPLAYABLE_IN_LIST;
import static org.cmdbuild.service.rest.constants.Serialization.EDITOR_TYPE;
import static org.cmdbuild.service.rest.constants.Serialization.FILTER;
import static org.cmdbuild.service.rest.constants.Serialization.GROUP;
import static org.cmdbuild.service.rest.constants.Serialization.INDEX;
import static org.cmdbuild.service.rest.constants.Serialization.INHERITED;
import static org.cmdbuild.service.rest.constants.Serialization.LENGTH;
import static org.cmdbuild.service.rest.constants.Serialization.LOOKUP_TYPE_NAME;
import static org.cmdbuild.service.rest.constants.Serialization.MANDATORY;
import static org.cmdbuild.service.rest.constants.Serialization.NAME;
import static org.cmdbuild.service.rest.constants.Serialization.PRECISION;
import static org.cmdbuild.service.rest.constants.Serialization.SCALE;
import static org.cmdbuild.service.rest.constants.Serialization.TARGET_CLASS;
import static org.cmdbuild.service.rest.constants.Serialization.TYPE;
import static org.cmdbuild.service.rest.constants.Serialization.UNIQUE;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = ATTRIBUTE_DETAIL)
public class AttributeDetail {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<AttributeDetail> {

		private String type;
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
		private Integer precision;
		private Integer scale;
		private String targetClass;
		private Integer length;
		private String editorType;
		private String lookupTypeName;
		private String filter;

		private Builder() {
			// use static method
		}

		@Override
		public AttributeDetail build() {
			return new AttributeDetail(this);
		}

		public Builder withType(final String type) {
			this.type = type;
			return this;
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

		public Builder withPrecision(final Integer precision) {
			this.precision = precision;
			return this;
		}

		public Builder withScale(final Integer scale) {
			this.scale = scale;
			return this;
		}

		public Builder withTargetClass(final String targetClass) {
			this.targetClass = targetClass;
			return this;
		}

		public Builder withLength(final Integer length) {
			this.length = length;
			return this;
		}

		public Builder withEditorType(final String editorType) {
			this.editorType = editorType;
			return this;
		}

		public Builder withLookupType(final String lookupTypeName) {
			this.lookupTypeName = lookupTypeName;
			return this;
		}

		public Builder withFilter(final String filter) {
			this.filter = filter;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private String type;
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
	private Integer precision;
	private Integer scale;
	private String targetClass;
	private Integer length;
	private String editorType;
	private String lookupTypeName;
	private String filter;

	AttributeDetail() {
		// package visibility
	}

	private AttributeDetail(final Builder builder) {
		this.type = builder.type;
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
		this.precision = builder.precision;
		this.scale = builder.scale;
		this.targetClass = builder.targetClass;
		this.length = builder.length;
		this.editorType = builder.editorType;
		this.lookupTypeName = builder.lookupTypeName;
		this.filter = builder.filter;
	}

	@XmlAttribute(name = TYPE)
	public String getType() {
		return type;
	}

	void setType(final String type) {
		this.type = type;
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

	@XmlAttribute(name = PRECISION)
	public Integer getPrecision() {
		return precision;
	}

	void setPrecision(final Integer precision) {
		this.precision = precision;
	}

	@XmlAttribute(name = SCALE)
	public Integer getScale() {
		return scale;
	}

	void setScale(final Integer scale) {
		this.scale = scale;
	}

	@XmlAttribute(name = TARGET_CLASS)
	public String getTargetClass() {
		return targetClass;
	}

	public void setTargetClass(final String targetClass) {
		this.targetClass = targetClass;
	}

	@XmlAttribute(name = LENGTH)
	public Integer getLength() {
		return length;
	}

	void setLength(final Integer length) {
		this.length = length;
	}

	@XmlAttribute(name = EDITOR_TYPE)
	public String getEditorType() {
		return editorType;
	}

	void setEditorType(final String editorType) {
		this.editorType = editorType;
	}

	@XmlAttribute(name = LOOKUP_TYPE_NAME)
	public String getLookupTypeName() {
		return lookupTypeName;
	}

	void setLookupTypeName(final String lookupTypeName) {
		this.lookupTypeName = lookupTypeName;
	}

	@XmlAttribute(name = FILTER)
	public String getFilter() {
		return filter;
	}

	void setFilter(final String filter) {
		this.filter = filter;
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
