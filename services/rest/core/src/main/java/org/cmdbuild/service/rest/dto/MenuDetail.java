package org.cmdbuild.service.rest.dto;

import static org.cmdbuild.service.rest.constants.Serialization.CHILDREN;
import static org.cmdbuild.service.rest.constants.Serialization.INDEX;
import static org.cmdbuild.service.rest.constants.Serialization.MENU_DETAIL;
import static org.cmdbuild.service.rest.constants.Serialization.OBJECT_DESCRIPTION;
import static org.cmdbuild.service.rest.constants.Serialization.OBJECT_ID;
import static org.cmdbuild.service.rest.constants.Serialization.OBJECT_TYPE;
import static org.cmdbuild.service.rest.constants.Serialization.TYPE;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement(name = MENU_DETAIL)
public class MenuDetail extends AbstractModel {

	private String type;
	private Long index;
	private String objectType;
	private String objectId;
	private String objectDescription;
	private List<MenuDetail> children;

	MenuDetail() {
		// package visibility
	}

	@XmlAttribute(name = TYPE)
	public String getType() {
		return type;
	}

	void setType(final String type) {
		this.type = type;
	}

	@XmlAttribute(name = INDEX)
	public Long getIndex() {
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
	public String getObjectDescription() {
		return objectDescription;
	}

	void setObjectDescription(final String description) {
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
	protected boolean doEquals(final Object obj) {
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
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(type) //
				.append(index) //
				.append(objectType) //
				.append(objectId) //
				.append(objectDescription) //
				.append(children) //
				.toHashCode();
	}

}
