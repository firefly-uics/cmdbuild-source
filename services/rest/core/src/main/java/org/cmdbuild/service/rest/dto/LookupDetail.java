package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.dto.Constants.ACTIVE;
import static org.cmdbuild.service.rest.dto.Constants.CODE;
import static org.cmdbuild.service.rest.dto.Constants.DEFAULT;
import static org.cmdbuild.service.rest.dto.Constants.DESCRIPTION;
import static org.cmdbuild.service.rest.dto.Constants.ID;
import static org.cmdbuild.service.rest.dto.Constants.LOOKUP_TYPE_DETAIL;
import static org.cmdbuild.service.rest.dto.Constants.NUMBER;
import static org.cmdbuild.service.rest.dto.Constants.PARENT_ID;
import static org.cmdbuild.service.rest.dto.Constants.PARENT_TYPE;
import static org.cmdbuild.service.rest.dto.Constants.TYPE;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@XmlRootElement(name = LOOKUP_TYPE_DETAIL)
public class LookupDetail {

	public static class Builder implements org.cmdbuild.common.Builder<LookupDetail> {

		private Long id;
		private String code;
		private String description;
		private String type;
		private Integer number;
		private boolean active;
		private boolean isDefault;
		private Long parentId;
		private String parentType;

		private Builder() {
			// use static method
		}

		@Override
		public LookupDetail build() {
			return new LookupDetail(this);
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

		public Builder withNumber(final Integer number) {
			this.number = number;
			return this;
		}

		public Builder thatIsActive(final boolean active) {
			this.active = active;
			return this;
		}

		public Builder thatIsDefault(final boolean isDefault) {
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
	private Integer number;
	private boolean active;
	private boolean isDefault;
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
	public Integer getNumber() {
		return number;
	}

	void setNumber(final Integer number) {
		this.number = number;
	}

	@XmlAttribute(name = ACTIVE)
	public boolean isActive() {
		return active;
	}

	void setActive(final boolean active) {
		this.active = active;
	}

	@XmlAttribute(name = DEFAULT)
	public boolean isDefault() {
		return isDefault;
	}

	void setDefault(final boolean isDefault) {
		this.isDefault = isDefault;
	}

	@XmlAttribute(name = PARENT_ID)
	public Long getParentId() {
		return parentId;
	}

	void setParentId(final Long parentId) {
		this.parentId = parentId;
	}

	public String getParentType() {
		return parentType;
	}

	@XmlAttribute(name = PARENT_TYPE)
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
		return id.equals(other.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
