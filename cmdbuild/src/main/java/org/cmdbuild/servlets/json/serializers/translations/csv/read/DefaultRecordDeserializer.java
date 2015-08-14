package org.cmdbuild.servlets.json.serializers.translations.csv.read;

import static org.apache.commons.lang3.StringUtils.*;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.IDENTIFIER;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.KEY_SEPARATOR;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.commonHeaders;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.createConverter;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.axis.utils.StringUtils;
import org.apache.commons.lang3.Validate;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.Converter;
import org.cmdbuild.servlets.json.schema.TranslatableElement;
import org.cmdbuild.servlets.json.translationtable.objects.csv.CsvTranslationRecord;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DefaultRecordDeserializer implements RecordDeserializer {

	private String type = EMPTY;
	private String identifier = EMPTY;
	private String owner = EMPTY;
	private String field = EMPTY;
	private final Map<String, String> translations = Maps.newHashMap();
	private final CsvTranslationRecord record;

	public DefaultRecordDeserializer(final Builder builder) {
		this.record = builder.record;
	}

	public static Builder newInstance() {
		return new Builder();
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<RecordDeserializer> {

		public CsvTranslationRecord record;

		@Override
		public RecordDeserializer build() {
			return new DefaultRecordDeserializer(this);
		}

		public Builder withRecord(final CsvTranslationRecord record) {
			this.record = record;
			return this;
		}

	}

	@Override
	public TranslationObject deserialize() {

		final String key = record.get(IDENTIFIER);
		unpack(key);
		// TODO log

		final TranslatableElement element = TO_ELEMENT_TYPE.apply(type);
		if (element == TranslatableElement.UNDEFINED) {
			// TODO log
		}

		final boolean contains = Iterables.contains(element.allowedFields(), field);
		if (!contains) {
			// TODO log
		}

		final Converter converter = createConverter(type, field);
		if (!converter.isValid()) {
			// TODO log
		}

		extractTranslations(record);

		final TranslationObject translationObject = converter //
				.withOwner(owner) //
				.withIdentifier(identifier) //
				.withTranslations(translations) //
				.create();

		return translationObject;
	}

	private void extractTranslations(final CsvTranslationRecord record) {
		final Set<String> headers = record.getKeySet();
		final Collection<String> languages = Lists.newArrayList();
		Iterables.addAll(languages, headers);
		Iterables.removeAll(languages, commonHeaders);
		for (final String language : languages) {
			translations.put(language, record.get(language));
		}
	}

	private void unpack(final String key) {
		validate(key);
		final String[] parts = StringUtils.split(key, KEY_SEPARATOR);
		type = parts[0];
		if (hasOwner(key)) {
			owner = parts[1];
			identifier = parts[2];
			field = parts[3];
		} else {
			identifier = parts[1];
			field = parts[2];
		}
	}

	private static boolean hasOwner(final String key) {
		return StringUtils.split(key, KEY_SEPARATOR).length == 4;
	}

	private static void validate(final String key) {
		Validate.notBlank(key);
		Validate.isTrue(StringUtils.split(key, KEY_SEPARATOR).length == 3
				|| StringUtils.split(key, KEY_SEPARATOR).length == 4);
	}

	private static final Function<String, TranslatableElement> TO_ELEMENT_TYPE = new Function<String, TranslatableElement>() {
		@Override
		public TranslatableElement apply(final String input) {
			return TranslatableElement.of(input);
		}
	};

}
