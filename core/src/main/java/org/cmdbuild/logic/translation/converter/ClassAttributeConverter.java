package org.cmdbuild.logic.translation.converter;

import java.util.Map;

import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.object.ClassAttributeDescription;
import org.cmdbuild.logic.translation.object.ClassAttributeGroup;

import com.google.common.collect.Maps;

public enum ClassAttributeConverter {

	DESCRIPTION("description") {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public TranslationObject create(final String classname, final String attributename) {
			org.cmdbuild.logic.translation.object.ClassAttributeDescription.Builder builder =  ClassAttributeDescription.newInstance() //
					.withClassname(classname) //
					.withAttributename(attributename);
			if(!translations.isEmpty()){
				builder.withTranslations(translations);
			}
			return builder.build();
		}

		@Override
		public ClassAttributeConverter withTranslations(Map<String, String> map) {
			translations = map;
			return this;
		}
	},

	GROUP("group") {

		@Override
		public TranslationObject create(final String classname, final String attributename) {
			org.cmdbuild.logic.translation.object.ClassAttributeGroup.Builder builder = ClassAttributeGroup.newInstance() //
					.withClassname(classname) //
					.withAttributename(attributename);
			if(!translations.isEmpty()){
				builder.withTranslations(translations);
			}
			return builder.build();
					
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public ClassAttributeConverter withTranslations(Map<String, String> map) {
			translations = map;
			return this;
		}

	},

	UNDEFINED("undefined") {

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public TranslationObject create(final String classname, final String attributename) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ClassAttributeConverter withTranslations(Map<String, String> map) {
			throw new UnsupportedOperationException();
		}
	};

	private final String fieldName;
	private static Map<String, String> translations = Maps.newHashMap();

	public abstract TranslationObject create(String classname, String attributename);
	
	public abstract ClassAttributeConverter withTranslations(Map<String, String> map);

	public abstract boolean isValid();

	private ClassAttributeConverter(final String fieldName) {
		this.fieldName = fieldName;
	}

	public static ClassAttributeConverter of(final String value) {
		for (final ClassAttributeConverter element : values()) {
			if (element.fieldName.equalsIgnoreCase(value)) {
				return element;
			}
		}
		return UNDEFINED;
	}

}
