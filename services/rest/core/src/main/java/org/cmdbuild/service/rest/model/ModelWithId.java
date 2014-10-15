package org.cmdbuild.service.rest.model;

import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_ID;

import javax.xml.bind.annotation.XmlAttribute;

import org.codehaus.jackson.annotate.JsonProperty;

public abstract class ModelWithId<I> extends Model {

	private I id;

	protected ModelWithId() {
		// usable by subclasses only
	}

	@XmlAttribute(name = UNDERSCORED_ID)
	@JsonProperty(UNDERSCORED_ID)
	public I getId() {
		return id;
	}

	protected void setId(final I id) {
		this.id = id;
	}

}