package org.cmdbuild.servlets.json.translation;

import static org.cmdbuild.servlets.json.CommunicationConstants.NAME;

import java.util.Collection;

import org.codehaus.jackson.annotate.JsonProperty;

public class JsonElement {
	private static final String FIELDS = "fields";
	private static final String CHILDREN = "children";
	private String name;
	private Collection<JsonField> fields;
	private Collection<JsonElement> children;

	@JsonProperty(NAME)
	public String getName() {
		return name;
	}
	
	@JsonProperty(FIELDS)
	public Collection<JsonField> getFields() {
		return fields;
	}

	@JsonProperty(CHILDREN)
	public Collection<JsonElement> getChildren() {
		return children;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setFields(final Collection<JsonField> fields) {
		this.fields = fields;
	}
	
	public void setChildren(Collection<JsonElement> children) {
		this.children = children;
	}

}
