package org.cmdbuild.logic.translation;

import java.util.Map;

public class AttributeClassTranslation extends BaseTranslation {

	private String attributeName;

	public AttributeClassTranslation(Builder builder) {
		this.setField(builder.field);
		this.setName(builder.className);
		this.setAttributeName(builder.attributeName);
		this.setTranslations(builder.translations);
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
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

		public Builder withField(String field) {
			this.field = field;
			return this;
		}

		public Builder withName(String attributeName) {
			this.attributeName = attributeName;
			return this;
		}

		public Builder forClass(String className) {
			this.className = className;
			return this;
		}

		public Builder withTranslations(Map<String, String> translations) {
			this.translations = translations;
			return this;
		}

	}

}
