package org.cmdbuild.logic.translation.converter;

import java.util.Map;

import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.object.LookupDescription;

import com.google.common.collect.Maps;

public enum LookupConverter {

	DESCRIPTION(description()) {

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public LookupConverter withTranslations(final Map<String, String> map) {
			translations = map;
			return this;
		}

		@Override
		public LookupDescription create(final String uuid) {
			final org.cmdbuild.logic.translation.object.LookupDescription.Builder builder = LookupDescription
					.newInstance() //
					.withUuid(uuid);

			if (!translations.isEmpty()) {
				builder.withTranslations(translations);
			}
			return builder.build();
		}
	},

	UNDEFINED("undefined") {

		@Override
		public boolean isValid() {
			return false;
		}

		@Override
		public LookupConverter withTranslations(final Map<String, String> map) {
			throw new UnsupportedOperationException();
		}

		@Override
		public LookupDescription create(final String name) {
			throw new UnsupportedOperationException();
		}
	};

	private final String fieldName;
	private static Map<String, String> translations = Maps.newHashMap();
	private static final String DESCRIPTION_FIELD = "description";

	public abstract TranslationObject create(String name);

	public static String description() {
		return DESCRIPTION_FIELD;
	}

	public abstract LookupConverter withTranslations(Map<String, String> map);

	public abstract boolean isValid();

	private LookupConverter(final String fieldName) {
		this.fieldName = fieldName;
	}

	public static LookupConverter of(final String value) {
		for (final LookupConverter element : values()) {
			if (element.fieldName.equalsIgnoreCase(value)) {
				return element;
			}
		}
		return UNDEFINED;
	}

}
