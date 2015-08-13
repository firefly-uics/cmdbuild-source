package org.cmdbuild.servlets.json.serializers.translations.csv;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.apache.commons.lang3.StringUtils.splitByCharacterTypeCamelCase;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.DEFAULT;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.DESCRIPTION;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.IDENTIFIER;
import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.KEY_SEPARATOR;

import java.util.Map;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.AttributeConverter;
import org.cmdbuild.logic.translation.converter.ClassConverter;
import org.cmdbuild.logic.translation.converter.Converter;
import org.cmdbuild.logic.translation.converter.DomainConverter;
import org.cmdbuild.logic.translation.converter.LookupConverter;
import org.cmdbuild.servlets.json.schema.TranslatableElement;
import org.cmdbuild.servlets.json.translationtable.objects.csv.CsvTranslationRecord;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class DefaultFieldSerializer implements FieldSerializer {

	private final String identifier;
	private final String owner;
	private final String fieldName;
	private final TranslatableElement element;
	private final TranslationLogic translationLogic;
	private final Iterable<String> enabledLanguages;
	private final DataAccessLogic dataLogic;
	private final LookupStore lookupStore;

	public static Builder newInstance() {
		return new Builder();
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<FieldSerializer> {

		private String identifier;
		private String owner;
		private String fieldName;
		private TranslatableElement element;
		private TranslationLogic translationLogic;
		private Iterable<String> enabledLanguages;
		private DataAccessLogic dataLogic;
		private LookupStore lookupStore;

		@Override
		public FieldSerializer build() {
			return new DefaultFieldSerializer(this);
		}

		public Builder withDataLogic(final DataAccessLogic dataLogic) {
			this.dataLogic = dataLogic;
			return this;
		}

		public Builder withElement(final TranslatableElement element) {
			this.element = element;
			return this;
		}

		public Builder withEnabledLanguages(final Iterable<String> enabledLanguages) {
			this.enabledLanguages = enabledLanguages;
			return this;
		}

		public Builder withFieldName(final String fieldName) {
			this.fieldName = fieldName;
			return this;
		}

		public Builder withIdentifier(final String identifier) {
			this.identifier = identifier;
			return this;
		}

		public Builder withOwner(final String owner) {
			this.owner = owner;
			return this;
		}

		public Builder withTranslationLogic(final TranslationLogic translationLogic) {
			this.translationLogic = translationLogic;
			return this;
		}

		public Builder withLookupStore(final LookupStore lookupStore) {
			this.lookupStore = lookupStore;
			return this;
		}

	}

	private DefaultFieldSerializer(final Builder builder) {
		this.element = builder.element;
		this.enabledLanguages = builder.enabledLanguages;
		this.fieldName = builder.fieldName;
		this.identifier = builder.identifier;
		this.owner = builder.owner;
		this.translationLogic = builder.translationLogic;
		this.dataLogic = builder.dataLogic;
		this.lookupStore = builder.lookupStore;
	}

	@Override
	public CsvTranslationRecord serialize() {
		final String key = buildKey();
		final String description = buildDescription();
		final Map<String, String> translations = readTranslations();
		final String defaultValue = fetchDefault();
		final CsvTranslationRecord record = writeRow(key, description, defaultValue, translations);
		return record;
	}

	// FIXME: do it better
	private String fetchDefault() {
		String defaultValue = EMPTY;
		if (element.equals(TranslatableElement.CLASS) && fieldName.equals(ClassConverter.description())) {
			defaultValue = dataLogic.findClass(identifier).getDescription();
		} else if (element.equals(TranslatableElement.ATTRIBUTECLASS)
				&& fieldName.equals(AttributeConverter.description())) {
			final CMClass ownerClass = dataLogic.findClass(owner);
			defaultValue = ownerClass.getAttribute(identifier).getDescription();
		} else if (element.equals(TranslatableElement.ATTRIBUTECLASS) && fieldName.equals(AttributeConverter.group())) {
			final CMClass ownerClass = dataLogic.findClass(owner);
			defaultValue = ownerClass.getAttribute(identifier).getGroup();
		} else if (element.equals(TranslatableElement.DOMAIN)) {
			if (fieldName.equals(DomainConverter.description())) {
				defaultValue = dataLogic.findDomain(identifier).getDescription();
			} else if (fieldName.equals(DomainConverter.directDescription())) {
				defaultValue = dataLogic.findDomain(identifier).getDescription1();
			} else if (fieldName.equals(DomainConverter.inverseDescription())) {
				defaultValue = dataLogic.findDomain(identifier).getDescription2();
			} else if (fieldName.equals(DomainConverter.masterDetail())) {
				defaultValue = dataLogic.findDomain(identifier).getMasterDetailDescription();
			}
		} else if (element.equals(TranslatableElement.LOOKUP_VALUE)) {
			if (fieldName.equals(LookupConverter.description())) {
				final Iterable<Lookup> storables = lookupStore.readFromUuid(identifier);
				if (Iterables.size(storables) > 1) {
					// TO DO : log
				}
				for (final Lookup lookup : storables) {
					defaultValue = lookup.getDescription();
					break; // there should be only one
				}
			}
		}
		return defaultIfBlank(defaultValue, EMPTY);
	}

	private Map<String, String> readTranslations() {
		final Converter converter = element.createConverter(fieldName);
		final TranslationObject translationObject = converter.withIdentifier(identifier).withOwner(owner).create();
		final Map<String, String> translations = translationLogic.readAll(translationObject);
		return translations;
	}

	private String buildKey() {
		String key = EMPTY;
		final String FORMAT_NO_OWNER = "%s%s%s%s%s";
		final String FORMAT_WITH_OWNER = "%s%s%s%s%s%s%s";
		if (isBlank(owner)) {
			key = String
					.format(FORMAT_NO_OWNER, element.getType(), KEY_SEPARATOR, identifier, KEY_SEPARATOR, fieldName);
		} else {
			key = String.format(FORMAT_WITH_OWNER, element.getType(), KEY_SEPARATOR, owner, KEY_SEPARATOR, identifier,
					KEY_SEPARATOR, fieldName);
		}
		return key;
	}

	private String buildDescription() {
		final String FORMAT = element.extendedDescriptionFormat();
		String description = EMPTY;
		if (isBlank(owner)) {
			description = String.format(FORMAT, lowerCase(join(splitByCharacterTypeCamelCase(fieldName), " ")),
					identifier);
		} else {
			description = String.format(FORMAT, lowerCase(join(splitByCharacterTypeCamelCase(fieldName), " ")),
					identifier, owner);
		}
		return description;
	}

	CsvTranslationRecord writeRow(final String key, final String description, final String defaultValue,
			final Map<String, String> translations) {
		final Map<String, Object> map = Maps.newHashMap();
		map.put(IDENTIFIER, key);
		map.put(DESCRIPTION, description);
		map.put(DEFAULT, defaultValue);
		for (final String language : enabledLanguages) {
			final String value = defaultIfNull(translations.get(language), EMPTY);
			map.put(language, value);
		}
		return new CsvTranslationRecord(map);
	}

}
