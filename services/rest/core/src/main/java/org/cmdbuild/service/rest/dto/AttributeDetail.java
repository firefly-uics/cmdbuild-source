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
import static org.cmdbuild.service.rest.constants.Serialization.PARAMS;
import static org.cmdbuild.service.rest.constants.Serialization.PRECISION;
import static org.cmdbuild.service.rest.constants.Serialization.SCALE;
import static org.cmdbuild.service.rest.constants.Serialization.TARGET_CLASS;
import static org.cmdbuild.service.rest.constants.Serialization.TEXT;
import static org.cmdbuild.service.rest.constants.Serialization.TYPE;
import static org.cmdbuild.service.rest.constants.Serialization.UNIQUE;

import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = ATTRIBUTE_DETAIL)
public class AttributeDetail {

	@XmlRootElement(name = FILTER)
	public static class Filter {

		public static class Builder implements org.apache.commons.lang3.builder.Builder<Filter> {

			private String text;
			private Map<String, String> params;

			private Builder() {
				// use factory method
			}

			@Override
			public Filter build() {
				validate();
				return new Filter(this);
			}

			private void validate() {
				// TODO Auto-generated method stub
			}

			public Filter.Builder withText(final String text) {
				this.text = text;
				return this;
			}

			public Filter.Builder withParams(final Map<String, String> params) {
				this.params = params;
				return this;
			}

		}

		public static Filter.Builder newInstance() {
			return new Builder();
		}

		private String text;
		private Map<String, String> params;

		Filter() {
			// package visibility
		}

		private Filter(final Filter.Builder builder) {
			this.text = builder.text;
			this.params = builder.params;
		}

		@XmlAttribute(name = TEXT)
		public String getText() {
			return text;
		}

		void setText(final String text) {
			this.text = text;
		}

		@XmlElementWrapper(name = PARAMS)
		public Map<String, String> getParams() {
			return params;
		}

		void setParams(final Map<String, String> params) {
			this.params = params;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof Filter)) {
				return false;
			}
			final Filter other = Filter.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.text, other.text) //
					.append(this.params, other.params) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(text) //
					.append(params) //
					.toHashCode();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
		}

	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<AttributeDetail> {

		private String type;
		private String name;
		private String description;
		private Boolean displayableInList;
		private Boolean unique;
		private Boolean mandatory;
		private Boolean inherited;
		private Boolean active;
		private Long index;
		private String defaultValue;
		private String group;
		private Long precision;
		private Long scale;
		private String targetClass;
		private Long length;
		private String editorType;
		private String lookupTypeName;
		private Filter filter;

		private Builder() {
			// use static method
		}

		@Override
		public AttributeDetail build() {
			validate();
			return new AttributeDetail(this);
		}

		private void validate() {
			// TODO Auto-generated method stub
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

		public Builder thatIsDisplayableInList(final Boolean displayableInList) {
			this.displayableInList = displayableInList;
			return this;
		}

		public Builder thatIsUnique(final Boolean unique) {
			this.unique = unique;
			return this;
		}

		public Builder thatIsMandatory(final Boolean mandatory) {
			this.mandatory = mandatory;
			return this;
		}

		public Builder thatIsInherited(final Boolean inherited) {
			this.inherited = inherited;
			return this;
		}

		public Builder thatIsActive(final Boolean active) {
			this.active = active;
			return this;
		}

		public Builder withIndex(final Long index) {
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

		public Builder withPrecision(final Long precision) {
			this.precision = precision;
			return this;
		}

		public Builder withScale(final Long scale) {
			this.scale = scale;
			return this;
		}

		public Builder withTargetClass(final String targetClass) {
			this.targetClass = targetClass;
			return this;
		}

		public Builder withLength(final Long length) {
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

		public Builder withFilter(final Filter filter) {
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
	private Boolean displayableInList;
	private Boolean unique;
	private Boolean mandatory;
	private Boolean inherited;
	private Boolean active;
	private Long index;
	private String defaultValue;
	private String group;
	private Long precision;
	private Long scale;
	private String targetClass;
	private Long length;
	private String editorType;
	private String lookupTypeName;
	private Filter filter;

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
	public Boolean isDisplayableInList() {
		return displayableInList;
	}

	void setDisplayableInList(final Boolean displayableInList) {
		this.displayableInList = displayableInList;
	}

	@XmlAttribute(name = UNIQUE)
	public Boolean isUnique() {
		return unique;
	}

	void setUnique(final Boolean unique) {
		this.unique = unique;
	}

	@XmlAttribute(name = MANDATORY)
	public Boolean isMandatory() {
		return mandatory;
	}

	void setMandatory(final Boolean mandatory) {
		this.mandatory = mandatory;
	}

	@XmlAttribute(name = INHERITED)
	public Boolean isInherited() {
		return inherited;
	}

	void setInherited(final Boolean inherited) {
		this.inherited = inherited;
	}

	@XmlAttribute(name = ACTIVE)
	public Boolean isActive() {
		return active;
	}

	void setActive(final Boolean active) {
		this.active = active;
	}

	@XmlAttribute(name = INDEX)
	public Long getIndex() {
		return index;
	}

	void setIndex(final Long index) {
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
	public Long getPrecision() {
		return precision;
	}

	void setPrecision(final Long precision) {
		this.precision = precision;
	}

	@XmlAttribute(name = SCALE)
	public Long getScale() {
		return scale;
	}

	void setScale(final Long scale) {
		this.scale = scale;
	}

	@XmlAttribute(name = TARGET_CLASS)
	public String getTargetClass() {
		return targetClass;
	}

	void setTargetClass(final String targetClass) {
		this.targetClass = targetClass;
	}

	@XmlAttribute(name = LENGTH)
	public Long getLength() {
		return length;
	}

	void setLength(final Long length) {
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

	@XmlElement(name = FILTER, nillable = true)
	public Filter getFilter() {
		return filter;
	}

	void setFilter(final Filter filter) {
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
		return new EqualsBuilder() //
				.append(this.type, other.type) //
				.append(this.name, other.name) //
				.append(this.description, other.description) //
				.append(this.displayableInList, other.displayableInList) //
				.append(this.unique, other.unique) //
				.append(this.mandatory, other.mandatory) //
				.append(this.inherited, other.inherited) //
				.append(this.active, other.active) //
				.append(this.index, other.index) //
				.append(this.defaultValue, other.defaultValue) //
				.append(this.group, other.group) //
				.append(this.precision, other.precision) //
				.append(this.scale, other.scale) //
				.append(this.targetClass, other.targetClass) //
				.append(this.length, other.length) //
				.append(this.editorType, other.editorType) //
				.append(this.lookupTypeName, other.lookupTypeName) //
				.append(this.filter, other.filter) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(this.type) //
				.append(this.name) //
				.append(this.description) //
				.append(this.displayableInList) //
				.append(this.unique) //
				.append(this.mandatory) //
				.append(this.inherited) //
				.append(this.active) //
				.append(this.index) //
				.append(this.defaultValue) //
				.append(this.group) //
				.append(this.precision) //
				.append(this.scale) //
				.append(this.targetClass) //
				.append(this.length) //
				.append(this.editorType) //
				.append(this.lookupTypeName) //
				.append(this.filter) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
