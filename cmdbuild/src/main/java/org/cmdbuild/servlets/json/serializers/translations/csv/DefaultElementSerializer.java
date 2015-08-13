package org.cmdbuild.servlets.json.serializers.translations.csv;

import java.util.Collection;

import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.servlets.json.schema.TranslatableElement;
import org.cmdbuild.servlets.json.translationtable.objects.csv.CsvTranslationRecord;

import com.google.common.collect.Lists;

public abstract class DefaultElementSerializer implements ElementSerializer {

	Iterable<String> enabledLanguages;
	TranslationLogic translationLogic;
	DataAccessLogic dataLogic;
	LookupStore lookupStore;

	@Override
	public abstract Collection<? extends CsvTranslationRecord> serialize();

	Collection<CsvTranslationRecord> serializeFields(final String owner, final String identifier,
			final TranslatableElement elementType) {
		final Collection<CsvTranslationRecord> records = Lists.newArrayList();
		final Iterable<String> allowedFields = elementType.allowedFields();
		for (final String fieldName : allowedFields) {
			final CsvTranslationRecord record = DefaultFieldSerializer.newInstance() //
					.withElement(elementType) //
					.withFieldName(fieldName) //
					.withIdentifier(identifier) //
					.withOwner(owner) //
					.withTranslationLogic(translationLogic) //
					.withEnabledLanguages(enabledLanguages) //
					.withDataLogic(dataLogic) //
					.withLookupStore(lookupStore) //
					.build() //
					.serialize();
			records.add(record);
		}
		return records;
	}

}
