package org.cmdbuild.servlets.json.translationtable;

import java.util.Collection;
import java.util.Map;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.LookupConverter;
import org.cmdbuild.servlets.json.management.JsonResponse;

import com.google.common.collect.Lists;

public class LookupTranslationSerializer implements TranslationSerializer {

	final TranslationLogic translationLogic;
	final boolean activeOnly;
	final LookupStore lookupStore;

	public LookupTranslationSerializer(final LookupStore lookupStore, final boolean activeOnly,
			final TranslationLogic translationLogic) {
		this.lookupStore = lookupStore;
		this.activeOnly = activeOnly;
		this.translationLogic = translationLogic;
	}

	@Override
	public JsonResponse serialize() {
		final Iterable<org.cmdbuild.data.store.lookup.LookupType> allTypes = lookupStore.readAllTypes();
		final Collection<JsonLookupType> jsonLookupTypes = Lists.newArrayList();
		for (final org.cmdbuild.data.store.lookup.LookupType type : allTypes) {
			final Iterable<Lookup> valuesOfType = lookupStore.readAll(type);
			final Collection<JsonLookupValue> jsonValues = Lists.newArrayList();
			final JsonLookupType jsonType = new JsonLookupType();
			jsonType.setDescription(type.name);
			for (final Lookup value : valuesOfType) {
				final JsonLookupValue jsonValue = new JsonLookupValue();
				final String code = value.code();
				final String uuid = value.getTranslationUuid();
				jsonValue.setCode(code);
				jsonValue.setTranslationUuid(uuid);
				final Collection<JsonField> jsonFields = readFields(value);
				jsonValue.setFields(jsonFields);
				jsonValues.add(jsonValue);
			}
			jsonType.setValues(jsonValues);
			jsonLookupTypes.add(jsonType);
		}
		return JsonResponse.success(jsonLookupTypes);
	}

	private Collection<JsonField> readFields(final Lookup value) {
		final Collection<JsonField> jsonFields = Lists.newArrayList();
		final TranslationObject translationObject = LookupConverter.DESCRIPTION //
				.withIdentifier(value.getTranslationUuid()) //
				.create();
		final Map<String, String> fieldTranslations = translationLogic.readAll(translationObject);
		final JsonField field = new JsonField();
		field.setName(LookupConverter.description());
		field.setTranslations(fieldTranslations);
		field.setValue(value.getDescription());
		jsonFields.add(field);
		return jsonFields;
	}
}
