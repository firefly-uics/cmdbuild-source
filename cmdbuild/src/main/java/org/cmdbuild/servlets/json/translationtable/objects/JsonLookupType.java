package org.cmdbuild.servlets.json.translationtable.objects;

import static org.cmdbuild.servlets.json.CommunicationConstants.CODE;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.VALUES;

import java.util.Collection;

import org.codehaus.jackson.annotate.JsonProperty;

public class JsonLookupType {

	private String code;
	private String description;
	private Collection<JsonLookupValue> values;

	@JsonProperty(CODE)
	public String getCode() {
		return code;
	}

	@JsonProperty(DESCRIPTION)
	public String getDescription() {
		return description;
	}

	@JsonProperty(VALUES)
	public Collection<JsonLookupValue> getValues() {
		return values;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setValues(final Collection<JsonLookupValue> values) {
		this.values = values;
	}
}
