package org.cmdbuild.logic.translation;

import java.util.Map;

public class ClassDescription extends BaseTranslation {

	private ClassDescription(final Builder builder) {
		this.setName(builder.name);
		this.setTranslations(builder.translations);
	}

	private static Builder newInstance() {
		return new Builder();
	}

	public static ClassDescription classDescription(final String name, final Map<String, String> translations) {
		return newInstance().withField(name).withTranslations(translations).build();
	}

	@Override
	public void accept(final TranslationObjectVisitor visitor) {
		visitor.visit(this);
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ClassDescription> {

		private String name;
		private Map<String, String> translations;

		private Builder() {
		}

		@Override
		public ClassDescription build() {
			return new ClassDescription(this);
		}

		public Builder withField(final String name) {
			this.name = name;
			return this;
		}

		public Builder withTranslations(final Map<String, String> translations) {
			this.translations = translations;
			return this;
		}

	}

	public enum ClassDescriptionConverter {

		DESCRIPTION("description") {

			@Override
			public ClassDescription create(final String name, final Map<String, String> translations) {
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
			public ClassDescription create(final String name, final Map<String, String> translations) {
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

		public abstract ClassDescription create(String name, Map<String, String> translations);

		public abstract ClassDescription create(String name);

		public abstract boolean isValid();

		private ClassDescriptionConverter(final String fieldName) {
			this.fieldName = fieldName;
		}

		public static ClassDescriptionConverter of(final String value) {
			for (final ClassDescriptionConverter element : values()) {
				if (element.fieldName.equalsIgnoreCase(value)) {
					return element;
				}
			}
			return UNDEFINED;
		}

	}

}
