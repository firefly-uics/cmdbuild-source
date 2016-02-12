package org.cmdbuild.logic.translation.converter;

import java.util.Map;

import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.object.ClassDescription;

import com.google.common.collect.Maps;

public enum ClassConverter {

	DESCRIPTION(description()) {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public ClassConverter withTranslations(final Map<String, String> map) {
			translations = map;
			return this;
		}

		@Override
		public ClassDescription create(final String name) {
			final org.cmdbuild.logic.translation.object.ClassDescription.Builder builder = ClassDescription
					.newInstance() //
					.withClassName(name);

			if (!translations.isEmpty()) {
				builder.withTranslations(translations);
			}
			return builder.build();
		}
	},

	UNDEFINED(undefined()) {

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public ClassConverter withTranslations(final Map<String, String> map) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ClassDescription create(final String name) {
			throw new UnsupportedOperationException();
		}
	};

	private final String fieldName;
	private static Map<String, String> translations = Maps.newHashMap();
	
	private static final String DESCRIPTION_FIELD = "description";
	private static final String UNDEFINED_FIELD = "undefined";

	public abstract TranslationObject create(String name);

	public abstract ClassConverter withTranslations(Map<String, String> map);

	public abstract boolean isValid();
	
	public static String description() {
		return DESCRIPTION_FIELD;
	}
	
	private static String undefined() {
		return UNDEFINED_FIELD;
	}

	private ClassConverter(final String fieldName) {
		this.fieldName = fieldName;
	}

	public static ClassConverter of(final String value) {
		for (final ClassConverter element : values()) {
			if (element.fieldName.equalsIgnoreCase(value)) {
				return element;
			}
		}
		return UNDEFINED;
	}

}
