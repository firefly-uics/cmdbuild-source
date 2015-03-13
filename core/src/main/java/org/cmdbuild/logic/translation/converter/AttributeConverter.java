package org.cmdbuild.logic.translation.converter;

import java.util.Map;

import org.cmdbuild.logic.translation.NullTranslationObject;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.object.ClassAttributeDescription;
import org.cmdbuild.logic.translation.object.ClassAttributeGroup;
import org.cmdbuild.logic.translation.object.DomainAttributeDescription;

import com.google.common.collect.Maps;

public enum AttributeConverter {
	
	CLASSATTRIBUTE_DESCRIPTION(aClass(), description()) {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public TranslationObject create(final String className, final String attributeName) {
			org.cmdbuild.logic.translation.object.ClassAttributeDescription.Builder builder = ClassAttributeDescription
					.newInstance() //
					.withClassname(className) //
					.withAttributename(attributeName);
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

	DOMAINATTRIBUTE_DESCRIPTION(aDomain(),  description()) {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public TranslationObject create(final String domainName, final String attributeName) {
			DomainAttributeDescription.Builder builder = DomainAttributeDescription
					.newInstance() //
					.withDomainName(domainName) //
					.withAttributeName(attributeName);
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

	CLASSATTRIBUTE_GROUP(aClass(), group()) {

		@Override
		public TranslationObject create(final String className, final String attributeName) {
			org.cmdbuild.logic.translation.object.ClassAttributeGroup.Builder builder = ClassAttributeGroup
					.newInstance() //
					.withClassname(className) //
					.withAttributename(attributeName);
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

	DOMAINATTRIBUTE_GROUP(aDomain(), group()) {

		@Override
		public TranslationObject create(final String domainName, final String attributeName) {
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
		public TranslationObject create(final String entryTypeName, final String attributeName) {
			throw new UnsupportedOperationException();
		}

		@Override
		public AttributeConverter withTranslations(Map<String, String> map) {
			throw new UnsupportedOperationException();
		}
	};
	
	private static final String CLASS = "class";
	private static final String DOMAIN = "domain";
	private static final String DESCRIPTION = "description";
	private static final String GROUP = "group";
	private final String fieldName;
	private final String entryType;
	private static Map<String, String> translations = Maps.newHashMap();

	public abstract TranslationObject create(String entryTypeName, String attributeName);
	public abstract AttributeConverter withTranslations(Map<String, String> map);
	public abstract boolean isValid();

	public static String description() {
		return DESCRIPTION;
	}
	
	public static String group() {
		return GROUP;
	}
	
	public static String aClass(){
		return CLASS;
	}
	
	public static String aDomain(){
		return DOMAIN;
	}
	
	
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
