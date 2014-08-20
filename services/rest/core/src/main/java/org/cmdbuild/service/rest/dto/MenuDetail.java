package org.cmdbuild.service.rest.dto;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.service.rest.constants.Serialization.CHILDREN;
import static org.cmdbuild.service.rest.constants.Serialization.INDEX;
import static org.cmdbuild.service.rest.constants.Serialization.MENU_DETAIL;
import static org.cmdbuild.service.rest.constants.Serialization.OBJECT_DESCRIPTION;
import static org.cmdbuild.service.rest.constants.Serialization.OBJECT_ID;
import static org.cmdbuild.service.rest.constants.Serialization.OBJECT_TYPE;
import static org.cmdbuild.service.rest.constants.Serialization.TYPE;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.google.common.collect.Lists;

@XmlRootElement(name = MENU_DETAIL)
public class MenuDetail {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<MenuDetail> {

		private static final Iterable<MenuDetail> NO_CHILDREN = Collections.emptyList();

		private String type;
		private Long index;
		private String objectType;
		private String objectId;
		private String objectDescription;
		private Iterable<MenuDetail> children;

		private Builder() {
			// use static method
		}

		@Override
		public MenuDetail build() {
			validate();
			return new MenuDetail(this);
		}

		private void validate() {
			index = defaultIfNull(index, 0L);
			children = defaultIfNull(children, NO_CHILDREN);
		}

		public Builder withType(final String type) {
			this.type = type;
			return this;
		}

		public Builder withIndex(final Long index) {
			this.index = index;
			return this;
		}

		public Builder withIndex(final Integer index) {
			return withIndex(index.longValue());
		}

		public Builder withObjectType(final String objectType) {
			this.objectType = objectType;
			return this;
		}

		public Builder withObjectId(final String objectId) {
			this.objectId = objectId;
			return this;
		}

		public Builder withObjectDescription(final String objectDescription) {
			this.objectDescription = objectDescription;
			return this;
		}

		public Builder withChildren(final Iterable<MenuDetail> children) {
			this.children = children;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private String type;
	private long index;
	private String objectType;
	private String objectId;
	private String objectDescription;
	private List<MenuDetail> children;

	MenuDetail() {
		// package visibility
	}

	private MenuDetail(final Builder builder) {
		this.type = builder.type;
		this.index = builder.index;
		this.objectType = builder.objectType;
		this.objectId = builder.objectId;
		this.objectDescription = builder.objectDescription;
		this.children = Lists.newArrayList(builder.children);
	}

	@XmlAttribute(name = TYPE)
	public String getType() {
		return type;
	}

	void setType(final String type) {
		this.type = type;
	}

	@XmlAttribute(name = INDEX)
	public long getIndex() {
		return index;
	}

	void setIndex(final Long index) {
		this.index = index;
	}

	@XmlAttribute(name = OBJECT_TYPE)
	public String getObjectType() {
		return objectType;
	}

	void setObjectType(final String objectType) {
		this.objectType = objectType;
	}

	@XmlAttribute(name = OBJECT_ID)
	public String getObjectId() {
		return objectId;
	}

	void setObjectId(final String objectId) {
		this.objectId = objectId;
	}

	@XmlAttribute(name = OBJECT_DESCRIPTION)
	public String getDescription() {
		return objectDescription;
	}

	void setDescription(final String description) {
		this.objectDescription = description;
	}

	@XmlElement(name = CHILDREN, type = MenuDetail.class)
	public List<MenuDetail> getChildren() {
		return children;
	}

	void setChildren(final List<MenuDetail> children) {
		this.children = children;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof MenuDetail)) {
			return false;
		}

		final MenuDetail other = MenuDetail.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.type, other.type) //
				.append(this.index, other.index) //
				.append(this.objectType, other.objectType) //
				.append(this.objectId, other.objectId) //
				.append(this.objectDescription, other.objectDescription) //
				.append(this.children, other.children) //
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(type) //
				.append(index) //
				.append(objectType) //
				.append(objectId) //
				.append(objectDescription) //
				.append(children) //
				.toHashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
