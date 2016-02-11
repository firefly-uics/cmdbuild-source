package org.cmdbuild.logic.translation;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Collections;
import java.util.Map;

public abstract class BaseTranslation implements TranslationObject {

	private static final Map<String, String> NO_TRANSLATIONS = Collections.emptyMap();

	private String name;

	@Deprecated
	private String field;

	private Map<String, String> translations;

	@Override
	public abstract void accept(TranslationObjectVisitor visitor);

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Deprecated
	public String getField() {
		return field;
	}

	public void setField(final String field) {
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
