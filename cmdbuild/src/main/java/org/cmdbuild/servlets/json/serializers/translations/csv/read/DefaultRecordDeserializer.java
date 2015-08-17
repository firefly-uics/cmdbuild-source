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
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.Converter;
import org.cmdbuild.servlets.json.schema.TranslatableElement;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;
import org.cmdbuild.servlets.json.translationtable.objects.csv.CsvTranslationRecord;
import org.slf4j.Logger;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static org.apache.commons.lang3.Validate.*;

public class DefaultRecordDeserializer implements RecordDeserializer {

	private static final Logger logger = Log.CMDBUILD;
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
	public TranslationSerialization getInput() {
		return record;
	}

	@Override
	public TranslationObject deserialize() {
		logger.info("parsing record '%s'", record.toString());
		final String key = record.get(IDENTIFIER);
		unpack(key);
		logger.debug("identifier deserialized to type: '{}' owner: '{}' identifier: '{}' field: '{}'", type, owner,
				identifier, field);

		final TranslatableElement element = TO_ELEMENT_TYPE.apply(type);
		isTrue(!element.equals(TranslatableElement.UNDEFINED), "unsupported type '" + type + "'");

		final boolean contains = Iterables.contains(element.allowedFields(), field);
		isTrue(contains, "unsupported field '" + field + "'");

		final Converter converter = createConverter(type, field);
		isTrue(converter.isValid(), "unsupported type and field pair '" + type + "' '" + field + "'");

		extractTranslations(record);
		logger.debug("translations: '{}'" + translations);

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
		notBlank(key, "missing identifier");
		isTrue(StringUtils.split(key, KEY_SEPARATOR).length == 3 //
				|| StringUtils.split(key, KEY_SEPARATOR).length == 4, //
				"unsupported identifier '" + key + "'");
	}

	private static final Function<String, TranslatableElement> TO_ELEMENT_TYPE = new Function<String, TranslatableElement>() {
		@Override
		public TranslatableElement apply(final String input) {
			return TranslatableElement.of(input);
		}
	};

}
