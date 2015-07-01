package org.cmdbuild.servlets.json.translationtable.objects;

import java.util.Collection;

import org.codehaus.jackson.annotate.JsonProperty;

public class JsonElementWithAttributes extends JsonElement {

	private static final String ATTRIBUTES = "attributes";

	private Collection<JsonElement> attributes;

	@JsonProperty(ATTRIBUTES)
	public Collection<JsonElement> getAttributes() {
		return attributes;
	}

	public void setAttributes(final Collection<JsonElement> attributes) {
		this.attributes = attributes;
	}

}
