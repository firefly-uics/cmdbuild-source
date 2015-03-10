package org.cmdbuild.logic.translation;

import java.util.Map;

public class ClassAttributeDescription extends BaseTranslation {

	private String className;

	private ClassAttributeDescription(final Builder builder) {
		this.setClassName(builder.className);
		this.setName(builder.attributeName);
		this.setTranslations(builder.translations);
	}

	static Builder newInstance() {
		return new Builder();
	}

	private void setClassName(final String className) {
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	@Override
	public void accept(final TranslationObjectVisitor visitor) {
		visitor.visit(this);
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ClassAttributeDescription> {

		private String className;
		private String attributeName;
		private Map<String, String> translations;

		private Builder() {
		}

		@Override
		public ClassAttributeDescription build() {
			return new ClassAttributeDescription(this);
		}

		public Builder withClassname(final String className) {
			this.className = className;
			return this;
		}

		public Builder withAttributename(final String attributeName) {
			this.attributeName = attributeName;
			return this;
		}

		public Builder withTranslations(final Map<String, String> translations) {
			this.translations = translations;
			return this;
		}

	}

}
