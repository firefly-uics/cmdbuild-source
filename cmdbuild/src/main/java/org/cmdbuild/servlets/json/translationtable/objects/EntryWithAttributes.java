package org.cmdbuild.servlets.json.translationtable.objects;

import java.util.Collection;

import org.codehaus.jackson.annotate.JsonProperty;

@Deprecated
// use JsonElementWithChildren instead
public class EntryWithAttributes extends TableEntry {

	private static final String ATTRIBUTES = "attributes";

	private Collection<TableEntry> attributes;

	@JsonProperty(ATTRIBUTES)
	public Collection<TableEntry> getAttributes() {
		return attributes;
	}

	public void setAttributes(final Collection<TableEntry> attributes) {
		this.attributes = attributes;
	}

}
