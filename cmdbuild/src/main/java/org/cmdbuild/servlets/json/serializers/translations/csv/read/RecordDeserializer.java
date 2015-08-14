package org.cmdbuild.servlets.json.serializers.translations.csv.read;

import org.cmdbuild.logic.translation.TranslationObject;

public interface RecordDeserializer {
	
	TranslationObject deserialize();

}
