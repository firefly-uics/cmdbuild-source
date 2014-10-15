package org.cmdbuild.service.rest.model;

import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_TYPE;

import javax.xml.bind.annotation.XmlAttribute;

import org.codehaus.jackson.annotate.JsonProperty;

public abstract class ModelWithIdAndType<I, T> extends ModelWithId<I> {

	private T type;

	protected ModelWithIdAndType() {
		// usable by subclasses only
	}

	@XmlAttribute(name = UNDERSCORED_TYPE)
	@JsonProperty(UNDERSCORED_TYPE)
	public T getType() {
		return type;
	}

	protected void setType(final T type) {
		this.type = type;
	}

}
