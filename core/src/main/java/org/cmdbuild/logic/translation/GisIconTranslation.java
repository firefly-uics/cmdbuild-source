package org.cmdbuild.logic.translation;

import java.util.Map;

public class GisIconTranslation extends BaseTranslation {

	public GisIconTranslation(final Builder builder) {
		this.setField(builder.field);
		this.setName(builder.name);
		this.setTranslations(builder.translations);
	}

	public static Builder newInstance() {
		return new Builder();
	}

	@Override
	public void accept(final TranslationObjectVisitor visitor) {
		visitor.visit(this);
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<GisIconTranslation> {

		private String name;
		private String field;
		private Map<String, String> translations;

		@Override
		public GisIconTranslation build() {
			return new GisIconTranslation(this);
		}

		public Builder withField(final String field) {
			this.field = field;
			return this;
		}

		public Builder withName(final String name) {
			this.name = name;
			return this;
		}

		public Builder withTranslations(final Map<String, String> translations) {
			this.translations = translations;
			return this;
		}

	}

}