package org.cmdbuild.logic.translation;

import java.util.Collections;
import java.util.Map;

public class NullTranslationObject implements TranslationObject {

	private static final Map<String, String> NO_TRANSLATIONS = Collections.emptyMap();

	public void accept(TranslationObjectVisitor visitor) {
		visitor.visit(this);
	};

	@Override
	public Map<String, String> getTranslations() {
		return NO_TRANSLATIONS;
	}

}