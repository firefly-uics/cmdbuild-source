package org.cmdbuild.logic.translation;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.Collections;
import java.util.Map;

public class DomainTranslation implements TranslationObject {

	private static final Map<String, String> NO_TRANSLATIONS = Collections.emptyMap();

	private String name;
	private String field;
	private Map<String, String> translations;

	@Override
	public void accept(final TranslationObjectVisitor visitor) {
		visitor.visit(this);
	}

	public String getName() {
		return name;
	}

	public void setName(final String className) {
		this.name = className;
	}

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
