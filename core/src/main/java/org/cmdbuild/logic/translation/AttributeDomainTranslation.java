package org.cmdbuild.logic.translation;

import java.util.Map;

public class AttributeDomainTranslation extends BaseTranslation {

	private String attributeName;

	public AttributeDomainTranslation(final Builder builder) {
		this.setField(builder.field);
		this.setName(builder.domainName);
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

	public static class Builder implements org.apache.commons.lang3.builder.Builder<AttributeDomainTranslation> {

		private String field;
		private String domainName;
		public String attributeName;
		public Map<String, String> translations;

		@Override
		public AttributeDomainTranslation build() {
			return new AttributeDomainTranslation(this);
		}

		public Builder withField(final String field) {
			this.field = field;
			return this;
		}

		public Builder withAttributeName(final String attributeName) {
			this.attributeName = attributeName;
			return this;
		}

		public Builder forDomain(final String domainName) {
			this.domainName = domainName;
			return this;
		}

		public Builder withTranslations(final Map<String, String> translations) {
			this.translations = translations;
			return this;
		}

	}

}
