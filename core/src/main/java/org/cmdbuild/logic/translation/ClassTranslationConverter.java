package org.cmdbuild.logic.translation;

import java.util.Map;

public enum ClassTranslationConverter {

	DESCRIPTION("description") {

		@Override
		public TranslationObject create(final String name, final Map<String, String> translations) {
			return ClassDescription.newInstance() //
					.withField(name) //
					.withTranslations(translations) //
					.build();
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public ClassDescription create(final String name) {
			return ClassDescription.newInstance() //
					.withField(name) //
					.build();
		}
	},

	UNDEFINED("undefined") {
		@Override
		public TranslationObject create(final String name, final Map<String, String> translations) {
			throw new IllegalArgumentException();
		}

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public ClassDescription create(final String name) {
			throw new IllegalArgumentException();
		}
	};

	private final String fieldName;

	public abstract TranslationObject create(String name, Map<String, String> translations);

	public abstract TranslationObject create(String name);

	public abstract boolean isValid();

	private ClassTranslationConverter(final String fieldName) {
		this.fieldName = fieldName;
	}

	public static ClassTranslationConverter of(final String value) {
		for (final ClassTranslationConverter element : values()) {
			if (element.fieldName.equalsIgnoreCase(value)) {
				return element;
			}
		}
		return UNDEFINED;
	}

}
