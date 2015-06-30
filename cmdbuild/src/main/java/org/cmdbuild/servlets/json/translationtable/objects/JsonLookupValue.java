package org.cmdbuild.servlets.json.translationtable.objects;

import static org.cmdbuild.servlets.json.CommunicationConstants.CODE;
import static org.cmdbuild.servlets.json.CommunicationConstants.TRANSLATION_UUID;

import java.util.Collection;

import org.codehaus.jackson.annotate.JsonProperty;

public class JsonLookupValue {

	private String code;
	private String translationUuid;
	private Collection<JsonField> fields;

	@JsonProperty(CODE)
	public String getDescription() {
		return code;
	}

	@JsonProperty(TRANSLATION_UUID)
	public String getTranslationUuid() {
		return translationUuid;
	}

	@JsonProperty("fields")
	public Collection<JsonField> getFields() {
		return fields;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public void setTranslationUuid(final String translationUuid) {
		this.translationUuid = translationUuid;
	}

	public void setFields(final Collection<JsonField> fields) {
		this.fields = fields;
	}

}
