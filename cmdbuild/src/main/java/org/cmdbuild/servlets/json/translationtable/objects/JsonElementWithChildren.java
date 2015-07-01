package org.cmdbuild.servlets.json.translationtable.objects;

import java.util.Collection;

import org.codehaus.jackson.annotate.JsonProperty;

public class JsonElementWithChildren extends JsonElement {

	private static final String CHILDREN = "children";

	private Collection<JsonElement> children;

	@JsonProperty(CHILDREN)
	public Collection<JsonElement> getChildren() {
		return children;
	}

	public void setChildren(final Collection<JsonElement> children) {
		this.children = children;
	}

}
