package org.cmdbuild.service.rest.model;

import static org.cmdbuild.service.rest.constants.Serialization.ID;

import javax.xml.bind.annotation.XmlAttribute;

public abstract class AbstractModelWithId extends AbstractModel {

	private Long id;

	protected AbstractModelWithId() {
		// usable by subclasses only
	}

	@XmlAttribute(name = ID)
	public Long getId() {
		return id;
	}

	protected void setId(final Long id) {
		this.id = id;
	}

}