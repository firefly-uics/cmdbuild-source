package org.cmdbuild.servlets.json.translationtable;

import static org.cmdbuild.servlets.json.CommunicationConstants.NAME;

import java.util.Collection;

import org.codehaus.jackson.annotate.JsonProperty;

public class JsonElement {
	private static final String FIELDS = "fields";
	private static final String ATTRIBUTES = "attributes";
	private String name;
	private Collection<JsonField> fields;
	private Collection<JsonElement> attributes;

	@JsonProperty(NAME)
	public String getName() {
		return name;
	}

	@JsonProperty(FIELDS)
	public Collection<JsonField> getFields() {
		return fields;
	}

	@JsonProperty(ATTRIBUTES)
	public Collection<JsonElement> getAttributes() {
		return attributes;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setFields(final Collection<JsonField> fields) {
		this.fields = fields;
	}

	public void setAttributes(final Collection<JsonElement> attributes) {
		this.attributes = attributes;
	}

}
