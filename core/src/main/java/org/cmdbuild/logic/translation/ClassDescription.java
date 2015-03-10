package org.cmdbuild.logic.translation;

import java.util.Map;

public class ClassDescription extends BaseTranslation {

	private ClassDescription(final Builder builder) {
		this.setName(builder.name);
		this.setTranslations(builder.translations);
	}

	static Builder newInstance() {
		return new Builder();
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

}
