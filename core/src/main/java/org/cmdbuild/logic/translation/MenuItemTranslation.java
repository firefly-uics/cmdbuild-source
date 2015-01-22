package org.cmdbuild.logic.translation;

import java.util.Map;

public class MenuItemTranslation extends BaseTranslation {

	@Override
	public void accept(final TranslationObjectVisitor visitor) {
		visitor.visit(this);
	}

	private MenuItemTranslation(final Builder builder) {
		this.setField(builder.field);
		this.setName(builder.name);
		this.setTranslations(builder.translations);
	}

	public static Builder newInstance() {
		return new Builder();
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<MenuItemTranslation> {

		private String name;
		private String field;
		private Map<String, String> translations;

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
		public MenuItemTranslation build() {
			return new MenuItemTranslation(this);
		}

		public Builder withTranslations(final Map<String, String> translations) {
			this.translations = translations;
			return this;
		}

	}

}
