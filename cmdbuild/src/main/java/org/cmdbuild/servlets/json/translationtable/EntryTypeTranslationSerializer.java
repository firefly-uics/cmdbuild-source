package org.cmdbuild.servlets.json.translationtable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.AttributeConverter;
import org.cmdbuild.logic.translation.converter.ClassConverter;
import org.cmdbuild.servlets.json.management.JsonResponse;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public abstract class EntryTypeTranslationSerializer implements TranslationSerializer {

	final DataAccessLogic dataLogic;
	final TranslationLogic translationLogic;
	final boolean activeOnly;

	Ordering<CMEntryType> entryTypeOrdering = EntryTypeSorter.DEFAULT.getOrientedOrdering();
	Ordering<CMAttribute> attributeOrdering = AttributeSorter.DEFAULT.getOrientedOrdering();

	EntryTypeTranslationSerializer(final DataAccessLogic dataLogic, final boolean activeOnly,
			final TranslationLogic translationLogic) {
		this.dataLogic = dataLogic;
		this.activeOnly = activeOnly;
		this.translationLogic = translationLogic;
	}

	static <T> Iterable<T> nullableIterable(final Iterable<T> it) {
		return it != null ? it : Collections.<T> emptySet();
	}

	@Override
	public abstract JsonResponse serialize();

	Iterable<? extends CMAttribute> sortAttributes(final Iterable<? extends CMAttribute> allAttributes) {
		final Iterable<? extends CMAttribute> sortedAttributes = attributeOrdering
				.sortedCopy(nullableIterable(allAttributes));
		return sortedAttributes;
	}

	Collection<JsonElement> serializeAttributes(final Iterable<? extends CMAttribute> attributes) {
		final Collection<JsonElement> attributesSerialization = Lists.newArrayList();
		for (final CMAttribute attribute : nullableIterable(attributes)) {
			final String attributeName = attribute.getName();
			final Collection<JsonField> attributeFields = readFields(attribute);
			final JsonElement jsonAttribute = new JsonElement();
			jsonAttribute.setName(attributeName);
			jsonAttribute.setFields(attributeFields);
			attributesSerialization.add(jsonAttribute);
		}
		return attributesSerialization;
	}

	Collection<JsonField> readFields(final CMClass cmclass) {
		final Collection<JsonField> jsonFields = Lists.newArrayList();
		final TranslationObject translationObject = ClassConverter.DESCRIPTION //
				.withIdentifier(cmclass.getName()) //
				.create();
		final Map<String, String> fieldTranslations = translationLogic.readAll(translationObject);
		final JsonField field = new JsonField();
		field.setName(ClassConverter.description());
		field.setTranslations(fieldTranslations);
		field.setValue(cmclass.getDescription());
		jsonFields.add(field);
		return jsonFields;
	}

	Collection<JsonField> readFields(final CMAttribute attribute) {
		final Collection<JsonField> jsonFields = Lists.newArrayList();
		final String ownerName = attribute.getOwner().getName();
		final TranslationObject translationObjectForDescription = AttributeConverter.CLASSATTRIBUTE_DESCRIPTION //
				.withOwner(ownerName).withIdentifier(attribute.getName()) //
				.create();
		final Map<String, String> descriptionTranslations = translationLogic.readAll(translationObjectForDescription);
		final JsonField descriptionField = new JsonField();
		descriptionField.setName(AttributeConverter.description());
		descriptionField.setTranslations(descriptionTranslations);
		descriptionField.setValue(attribute.getDescription());
		jsonFields.add(descriptionField);

		final TranslationObject translationObjectForGroup = AttributeConverter.CLASSATTRIBUTE_GROUP //
				.withOwner(ownerName).withIdentifier(attribute.getName()) //
				.create();
		final Map<String, String> groupTranslations = translationLogic.readAll(translationObjectForGroup);
		final JsonField groupField = new JsonField();
		groupField.setName(AttributeConverter.group());
		groupField.setTranslations(groupTranslations);
		groupField.setValue(attribute.getGroup());
		jsonFields.add(groupField);
		return jsonFields;
	}

}
