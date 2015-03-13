package org.cmdbuild.logic.translation.converter;

import java.util.Map;

import org.cmdbuild.logic.translation.NullTranslationObject;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.object.ClassAttributeDescription;
import org.cmdbuild.logic.translation.object.ClassAttributeGroup;
import org.cmdbuild.logic.translation.object.DomainAttributeDescription;

import com.google.common.collect.Maps;

public enum AttributeConverter {
	
	CLASSATTRIBUTE_DESCRIPTION("class", "description") {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public TranslationObject create(final String classname, final String attributename) {
			org.cmdbuild.logic.translation.object.ClassAttributeDescription.Builder builder = ClassAttributeDescription
					.newInstance() //
					.withClassname(classname) //
					.withAttributename(attributename);
			if (!translations.isEmpty()) {
				builder.withTranslations(translations);
			}
			return builder.build();
		}

		@Override
		public AttributeConverter withTranslations(Map<String, String> map) {
			translations = map;
			return this;
		}
	},

	DOMAINATTRIBUTE_DESCRIPTION("domain", "description") {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public TranslationObject create(final String entryTypeName, final String attributename) {
			DomainAttributeDescription.Builder builder = DomainAttributeDescription
					.newInstance() //
					.withDomainName(entryTypeName) //
					.withAttributeName(attributename);
			if (!translations.isEmpty()) {
				builder.withTranslations(translations);
			}
			return builder.build();
		}

		@Override
		public AttributeConverter withTranslations(Map<String, String> map) {
			translations = map;
			return this;
		}
	},

	CLASSATTRIBUTE_GROUP("class", "group") {

		@Override
		public TranslationObject create(final String classname, final String attributename) {
			org.cmdbuild.logic.translation.object.ClassAttributeGroup.Builder builder = ClassAttributeGroup
					.newInstance() //
					.withClassname(classname) //
					.withAttributename(attributename);
			if (!translations.isEmpty()) {
				builder.withTranslations(translations);
			}
			return builder.build();

		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public AttributeConverter withTranslations(Map<String, String> map) {
			translations = map;
			return this;
		}

	},

	DOMAINATTRIBUTE_GROUP("domain", "group") {

		@Override
		public TranslationObject create(final String entrtTypeName, final String attributename) {
			return new NullTranslationObject();
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public AttributeConverter withTranslations(Map<String, String> map) {
			translations = map;
			return this;
		}

	},

	UNDEFINED("undefined", null) {

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public TranslationObject create(final String classname, final String attributename) {
			throw new UnsupportedOperationException();
		}

		@Override
		public AttributeConverter withTranslations(Map<String, String> map) {
			throw new UnsupportedOperationException();
		}
	};
	
	

	private final String fieldName;
	private final String entryType;
	private static Map<String, String> translations = Maps.newHashMap();

	public abstract TranslationObject create(String classname, String attributename);

	public abstract AttributeConverter withTranslations(Map<String, String> map);

	public abstract boolean isValid();

	private AttributeConverter(final String entryType, String fieldName) {
		this.entryType = entryType;
		this.fieldName = fieldName;
	}

	public static AttributeConverter of(String entryType, final String value) {
		for (final AttributeConverter element : values()) {
			if (element.entryType.equalsIgnoreCase(entryType) && element.fieldName.equalsIgnoreCase(value)) {
				return element;
			}
		}
		return UNDEFINED;
	}

	public String fieldName() {
		return fieldName;
	}

	public String entryType() {
		return entryType;
	}
}
