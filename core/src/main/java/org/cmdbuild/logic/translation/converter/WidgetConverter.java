package org.cmdbuild.logic.translation.converter;

import java.util.Map;

import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.object.WidgetLabel;

import com.google.common.collect.Maps;

public enum WidgetConverter {

	LABEL(label()) {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public WidgetConverter withTranslations(final Map<String, String> map) {
			translations = map;
			return this;
		}

		@Override
		public WidgetLabel create(final String name) {
			final WidgetLabel.Builder builder = WidgetLabel //
					.newInstance() //
					.withClassName(name);

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
		public WidgetConverter withTranslations(final Map<String, String> map) {
			throw new UnsupportedOperationException();
		}

		@Override
		public WidgetLabel create(final String name) {
			throw new UnsupportedOperationException();
		}
	};

	private final String fieldName;
	private static Map<String, String> translations = Maps.newHashMap();

	private static final String LABEL_FIELD = "buttonlabel";
	private static final String UNDEFINED_FIELD = "undefined";

	public abstract TranslationObject create(String name);

	public abstract WidgetConverter withTranslations(Map<String, String> map);

	public abstract boolean isValid();

	public static String label() {
		return LABEL_FIELD;
	}

	private static String undefined() {
		return UNDEFINED_FIELD;
	}

	private WidgetConverter(final String fieldName) {
		this.fieldName = fieldName;
	}

	public static WidgetConverter of(final String value) {
		for (final WidgetConverter element : values()) {
			if (element.fieldName.equalsIgnoreCase(value)) {
				return element;
			}
		}
		return UNDEFINED;
	}

}
