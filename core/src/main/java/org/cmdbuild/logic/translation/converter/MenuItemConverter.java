package org.cmdbuild.logic.translation.converter;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.object.MenuItemDescription;

import com.google.common.collect.Maps;

public enum MenuItemConverter {

	DESCRIPTION(description()) {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public MenuItemConverter withTranslations(final Map<String, String> map) {
			translations = map;
			return this;
		}

		@Override
		public MenuItemDescription create(final String name) {
			Validate.notBlank(name, "missing identifier, identifier is needed for handling translation");
			final org.cmdbuild.logic.translation.object.MenuItemDescription.Builder builder = MenuItemDescription
					.newInstance() //
					.withUuid(name);

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
		public MenuItemConverter withTranslations(final Map<String, String> map) {
			throw new UnsupportedOperationException();
		}

		@Override
		public MenuItemDescription create(final String name) {
			throw new UnsupportedOperationException();
		}
	};

	private final String fieldName;
	private static Map<String, String> translations = Maps.newHashMap();
	private static final String DESCRIPTION_FIELD = "description";
	private static final String UNDEFINED_FIELD = "undefined";

	public abstract TranslationObject create(String name);

	public static String description() {
		return DESCRIPTION_FIELD;
	}

	private static String undefined() {
		return UNDEFINED_FIELD;
	}

	public abstract MenuItemConverter withTranslations(Map<String, String> map);

	public abstract boolean isValid();

	private MenuItemConverter(final String fieldName) {
		this.fieldName = fieldName;
	}

	public static MenuItemConverter of(final String value) {
		for (final MenuItemConverter element : values()) {
			if (element.fieldName.equalsIgnoreCase(value)) {
				return element;
			}
		}
		return UNDEFINED;
	}

}
