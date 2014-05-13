package org.cmdbuild.logic.translation;

import java.util.Map;

public class ClassTranslation extends BaseTranslation {

	private ClassTranslation(final Builder builder) {
		this.setField(builder.field);
		this.setName(builder.name);
		this.setTranslations(builder.tranlslations);
	}

	@Override
	public void accept(final TranslationObjectVisitor visitor) {
		visitor.visit(this);
	}

	public static Builder newInstance() {
		return new Builder();
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ClassTranslation> {

		private String name;
		private String field;
		private Map<String, String> tranlslations;

		private Builder() {
		}

		public Builder withName(final String name) {
			this.name = name;
			return this;
		}

		public Builder withField(final String field) {
			this.field = field;
			return this;
		}

		@Override
		public ClassTranslation build() {
			return new ClassTranslation(this);
		}

		public Builder withTranslations(final Map<String, String> translations) {
			this.tranlslations = translations;
			return this;
		}

	}

}
