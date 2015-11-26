package org.cmdbuild.logic.translation.converter;

import java.util.Map;

import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.object.InstanceName;
import org.cmdbuild.logic.translation.object.ViewDescription;

import com.google.common.collect.Maps;

public enum InstanceConverter {

	NAME(nameField()) {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public InstanceConverter withTranslations(final Map<String, String> map) {
			translations = map;
			return this;
		}

		@Override
		public InstanceName create(final String instanceName) {
			final InstanceName.Builder builder = InstanceName //
					.newInstance();

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
		public InstanceConverter withTranslations(final Map<String, String> map) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ViewDescription create(final String name) {
			throw new UnsupportedOperationException();
		}
	};

	private final String fieldName;
	private static Map<String, String> translations = Maps.newHashMap();

	private static final String NAME_FIELD = "instancename";
	private static final String UNDEFINED_FIELD = "undefined";

	public abstract TranslationObject create(String name);

	public abstract InstanceConverter withTranslations(Map<String, String> map);

	public abstract boolean isValid();

	public static String nameField() {
		return NAME_FIELD;
	}

	private static String undefined() {
		return UNDEFINED_FIELD;
	}

	private InstanceConverter(final String fieldName) {
		this.fieldName = fieldName;
	}

	public static InstanceConverter of(final String value) {
		for (final InstanceConverter element : values()) {
			if (element.fieldName.equalsIgnoreCase(value)) {
				return element;
			}
		}
		return UNDEFINED;
	}

}
