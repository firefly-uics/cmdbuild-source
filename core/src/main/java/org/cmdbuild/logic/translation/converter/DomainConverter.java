package org.cmdbuild.logic.translation.converter;

import java.util.Map;

import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.object.DomainDescription;
import org.cmdbuild.logic.translation.object.DomainDirectDescription;
import org.cmdbuild.logic.translation.object.DomainInverseDescription;
import org.cmdbuild.logic.translation.object.DomainMasterDetailLabel;

import com.google.common.collect.Maps;

public enum DomainConverter {

	DESCRIPTION(description()) {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public TranslationObject create(final String domainName) {
			org.cmdbuild.logic.translation.object.DomainDescription.Builder builder =  DomainDescription.newInstance() //
					.withDomainName(domainName);
			if(!translations.isEmpty()){
				builder.withTranslations(translations);
			}
			return builder.build();
		}

		@Override
		public DomainConverter withTranslations(Map<String, String> map) {
			translations = map;
			return this;
		}
	},

	DIRECT_DESCRIPTION(directDescription()) {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public TranslationObject create(final String domainName) {
			org.cmdbuild.logic.translation.object.DomainDirectDescription.Builder builder =  DomainDirectDescription.newInstance() //
					.withDomainName(domainName);
			if(!translations.isEmpty()){
				builder.withTranslations(translations);
			}
			return builder.build();
		}

		@Override
		public DomainConverter withTranslations(Map<String, String> map) {
			translations = map;
			return this;
		}
	},

	INVERSE_DESCRIPTION(inverseDescription()) {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public TranslationObject create(final String domainName) {
			org.cmdbuild.logic.translation.object.DomainInverseDescription.Builder builder =  DomainInverseDescription.newInstance() //
					.withDomainName(domainName);
			if(!translations.isEmpty()){
				builder.withTranslations(translations);
			}
			return builder.build();
		}

		@Override
		public DomainConverter withTranslations(Map<String, String> map) {
			translations = map;
			return this;
		}
	},

	MASTERDETAIL_LABEL(masterDetail()) {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public TranslationObject create(final String domainName) {
			org.cmdbuild.logic.translation.object.DomainMasterDetailLabel.Builder builder =  DomainMasterDetailLabel.newInstance() //
					.withDomainName(domainName);
			if(!translations.isEmpty()){
				builder.withTranslations(translations);
			}
			return builder.build();
		}

		@Override
		public DomainConverter withTranslations(Map<String, String> map) {
			translations = map;
			return this;
		}
	},

	UNDEFINED(undefined()) {

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public TranslationObject create(final String domainName) {
			throw new UnsupportedOperationException();
		}

		@Override
		public DomainConverter withTranslations(Map<String, String> map) {
			throw new UnsupportedOperationException();
		}
	};

	private final String fieldName;
	private static Map<String, String> translations = Maps.newHashMap();
	private static final String DESCRIPTION_FIELD = "description";
	private static final String DIRECTDESCRIPTION = "directDescription";
	private static final String INVERSEDESCRIPTION = "inverseDescription";
	private static final String MASTERDETAIL = "masterDetail";
	private static final String UNDEFINED_FIELD = "undefined";

	public abstract TranslationObject create(String domainName);

	public abstract DomainConverter withTranslations(Map<String, String> map);

	public abstract boolean isValid();

	public static String description() {
		return DESCRIPTION_FIELD;
	}

	public static String directDescription() {
		return DIRECTDESCRIPTION;
	}

	public static String inverseDescription() {
		return INVERSEDESCRIPTION;
	}

	public static String masterDetail() {
		return MASTERDETAIL;
	}

	private static String undefined() {
		return UNDEFINED_FIELD;
	}

	private DomainConverter(final String fieldName) {
		this.fieldName = fieldName;
	}

	public static DomainConverter of(final String value) {
		for (final DomainConverter element : values()) {
			if (element.fieldName.equalsIgnoreCase(value)) {
				return element;
			}
		}
		return UNDEFINED;
	}

	public String field() {
		return fieldName;
	}

}
