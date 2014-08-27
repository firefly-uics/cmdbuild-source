package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.constants.Serialization.ACTIVE;
import static org.cmdbuild.service.rest.constants.Serialization.CODE;
import static org.cmdbuild.service.rest.constants.Serialization.DEFAULT;
import static org.cmdbuild.service.rest.constants.Serialization.DESCRIPTION;
import static org.cmdbuild.service.rest.constants.Serialization.ID;
import static org.cmdbuild.service.rest.constants.Serialization.LOOKUP_TYPE_DETAIL;
import static org.cmdbuild.service.rest.constants.Serialization.NUMBER;
import static org.cmdbuild.service.rest.constants.Serialization.PARENT_ID;
import static org.cmdbuild.service.rest.constants.Serialization.PARENT_TYPE;
import static org.cmdbuild.service.rest.constants.Serialization.TYPE;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement(name = LOOKUP_TYPE_DETAIL)
public class LookupDetail {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<LookupDetail> {

		private Long id;
		private String code;
		private String description;
		private String type;
		private Long number;
		private Boolean active;
		private Boolean isDefault;
		private Long parentId;
		private String parentType;

		private Builder() {
			// use static method
		}

		@Override
		public LookupDetail build() {
			validate();
			return new LookupDetail(this);
		}

		private void validate() {
			// TODO Auto-generated method stub
		}

		public Builder withId(final Long id) {
			this.id = id;
			return this;
		}

		public Builder withCode(final String code) {
			this.code = code;
			return this;
		}

		public Builder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public Builder withType(final String type) {
			this.type = type;
			return this;
		}

		public Builder withNumber(final Long number) {
			this.number = number;
			return this;
		}

		public Builder thatIsActive(final Boolean active) {
			this.active = active;
			return this;
		}

		public Builder thatIsDefault(final Boolean isDefault) {
			this.isDefault = isDefault;
			return this;
		}

		public Builder withParentId(final Long parentId) {
			this.parentId = parentId;
			return this;
		}

		public Builder withParentType(final String parentType) {
			this.parentType = parentType;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private Long id;
	private String code;
	private String description;
	private String type;
	private Long number;
	private Boolean active;
	private Boolean isDefault;
	private Long parentId;
	private String parentType;

	LookupDetail() {
		// package visibility
	}

	private LookupDetail(final Builder builder) {
		this.id = builder.id;
		this.code = builder.code;
		this.description = builder.description;
		this.type = builder.type;
		this.number = builder.number;
		this.active = builder.active;
		this.isDefault = builder.isDefault;
		this.parentId = builder.parentId;
		this.parentType = builder.parentType;
	}

	@XmlAttribute(name = ID)
	public Long getId() {
		return id;
	}

	void setId(final Long id) {
		this.id = id;
	}

	@XmlAttribute(name = CODE)
	public String getCode() {
		return code;
	}

	void setCode(final String code) {
		this.code = code;
	}

	@XmlAttribute(name = DESCRIPTION)
	public String getDescription() {
		return description;
	}

	void setDescription(final String description) {
		this.description = description;
	}

	@XmlAttribute(name = TYPE)
	public String getType() {
		return type;
	}

	void setType(final String type) {
		this.type = type;
	}

	@XmlAttribute(name = NUMBER)
	public Long getNumber() {
		return number;
	}

	void setNumber(final Long number) {
		this.number = number;
	}

	@XmlAttribute(name = ACTIVE)
	public Boolean isActive() {
		return active;
	}

	void setActive(final Boolean active) {
		this.active = active;
	}

	@XmlAttribute(name = DEFAULT)
	public Boolean isDefault() {
		return isDefault;
	}

	void setDefault(final Boolean isDefault) {
		this.isDefault = isDefault;
	}

	@XmlAttribute(name = PARENT_ID)
	@JsonProperty(PARENT_ID)
	public Long getParentId() {
		return parentId;
	}

	void setParentId(final Long parentId) {
		this.parentId = parentId;
	}

	@XmlAttribute(name = PARENT_TYPE)
	@JsonProperty(PARENT_TYPE)
	public String getParentType() {
		return parentType;
	}

	void setParentType(final String parentType) {
		this.parentType = parentType;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof LookupDetail)) {
			return false;
		}

		final LookupDetail other = LookupDetail.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.id, other.id) //
				.append(this.code, other.code) //
				.append(this.description, other.description) //
				.append(this.type, other.type) //
				.append(this.number, other.number) //
				.append(this.active, other.active) //
				.append(this.isDefault, other.isDefault) //
				.append(this.parentId, other.parentId) //
				.append(this.parentType, other.parentType) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(id) //
				.append(code) //
				.append(description) //
				.append(type) //
				.append(number) //
				.append(active) //
				.append(isDefault) //
				.append(parentId) //
				.append(parentType) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
