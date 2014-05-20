package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.logic.translation.TranslationObject;

public interface TranslationFacade {

	String read(TranslationObject translationObject);

}