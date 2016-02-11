package org.cmdbuild.logic.translation.converter;

import java.util.Map;

import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.object.ReportDescription;
import org.cmdbuild.logic.translation.object.ViewDescription;

import com.google.common.collect.Maps;

public enum ReportConverter {

	DESCRIPTION(description()) {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public ReportConverter withTranslations(final Map<String, String> map) {
			translations = map;
			return this;
		}

		@Override
		public ReportDescription create(final String name) {
			final ReportDescription.Builder builder = ReportDescription //
					.newInstance() //
					.withName(name);

			if (!translations.isEmpty()) {
				builder.withTranslations(translations);
			}
			return builder.build();
		}
	},

	UNDEFINED(undefined()) {

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public ReportConverter withTranslations(final Map<String, String> map) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ViewDescription create(final String name) {
			throw new UnsupportedOperationException();
		}
	};

	private final String fieldName;
	private static Map<String, String> translations = Maps.newHashMap();

	private static final String DESCRIPTION_FIELD = "description";
	private static final String UNDEFINED_FIELD = "undefined";

	public abstract TranslationObject create(String name);

	public abstract ReportConverter withTranslations(Map<String, String> map);

	public abstract boolean isValid();

	public static String description() {
		return DESCRIPTION_FIELD;
	}

	private static String undefined() {
		return UNDEFINED_FIELD;
	}

	private ReportConverter(final String fieldName) {
		this.fieldName = fieldName;
	}

	public static ReportConverter of(final String value) {
		for (final ReportConverter element : values()) {
			if (element.fieldName.equalsIgnoreCase(value)) {
				return element;
			}
		}
		return UNDEFINED;
	}

}
