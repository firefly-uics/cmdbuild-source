package org.cmdbuild.logic.translation;

import static org.apache.commons.lang3.ObjectUtils.*;

import java.util.Collections;
import java.util.Map;

public abstract class BaseTranslation implements TranslationObject {

	private static final Map<String, String> NO_TRANSLATIONS = Collections.emptyMap();

	private String name;
	private String field;
	private Map<String, String> translations;

	@Override
	public abstract void accept(TranslationObjectVisitor visitor);

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	@Override
	public Map<String, String> getTranslations() {
		return defaultIfNull(translations, NO_TRANSLATIONS);
	}

	public void setTranslations(final Map<String, String> translations) {
		this.translations = translations;
	}

}
