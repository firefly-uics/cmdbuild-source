package org.cmdbuild.logic.translation;

import java.util.Map;

public class AttributeClassTranslation extends BaseTranslation {

	private String attributeName;

	public AttributeClassTranslation(final Builder builder) {
		this.setField(builder.field);
		this.setName(builder.className);
		this.setAttributeName(builder.attributeName);
		this.setTranslations(builder.translations);
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(final String attributeName) {
		this.attributeName = attributeName;
	}

	@Override
	public void accept(final TranslationObjectVisitor visitor) {
		visitor.visit(this);
	}

	public static Builder newInstance() {
		return new Builder();
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<AttributeClassTranslation> {

		private String field;
		private String className;
		public String attributeName;
		public Map<String, String> translations;

		@Override
		public AttributeClassTranslation build() {
			return new AttributeClassTranslation(this);
		}

		public Builder withField(final String field) {
			this.field = field;
			return this;
		}

		public Builder withName(final String attributeName) {
			this.attributeName = attributeName;
			return this;
		}

		public Builder forClass(final String className) {
			this.className = className;
			return this;
		}

		public Builder withTranslations(final Map<String, String> translations) {
			this.translations = translations;
			return this;
		}

	}

}
