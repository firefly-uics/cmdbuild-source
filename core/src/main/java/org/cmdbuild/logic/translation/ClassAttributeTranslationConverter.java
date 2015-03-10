package org.cmdbuild.logic.translation;

import java.util.Map;

public enum ClassAttributeTranslationConverter {

	DESCRIPTION("description") {

		@Override
		public TranslationObject create(final String classname, final String attributename,
				final Map<String, String> translations) {
			return ClassAttributeDescription.newInstance() //
					.withClassname(classname) //
					.withAttributename(attributename).withTranslations(translations) //
					.build();
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public TranslationObject create(final String classname, final String attributename) {
			return ClassAttributeDescription.newInstance() //
					.withClassname(classname) //
					.withAttributename(attributename).build();
		}
	},

	GROUP("group") {

		@Override
		public TranslationObject create(final String classname, final String attributename,
				final Map<String, String> translations) {
			return ClassAttributeGroup.newInstance() //
					.withClassname(classname) //
					.withAttributename(attributename) //
					.withTranslations(translations).build();
		}

		@Override
		public TranslationObject create(final String classname, final String attributename) {
			return ClassAttributeGroup.newInstance() //
					.withClassname(classname) //
					.withAttributename(attributename) //
					.build();
		}

		@Override
		public boolean isValid() {
			return true;
		}

	},

	UNDEFINED("undefined") {
		@Override
		public TranslationObject create(final String classname, final String attributename,
				final Map<String, String> translations) {
			throw new IllegalArgumentException();
		}

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public TranslationObject create(final String classname, final String attributename) {
			throw new IllegalArgumentException();
		}
	};

	private final String fieldName;

	public abstract TranslationObject create(String classname, String attributename, Map<String, String> map);

	public abstract TranslationObject create(String classname, String attributename);

	public abstract boolean isValid();

	private ClassAttributeTranslationConverter(final String fieldName) {
		this.fieldName = fieldName;
	}

	public static ClassAttributeTranslationConverter of(final String value) {
		for (final ClassAttributeTranslationConverter element : values()) {
			if (element.fieldName.equalsIgnoreCase(value)) {
				return element;
			}
		}
		return UNDEFINED;
	}

}
