package org.cmdbuild.servlets.json.serializers.translations.table;

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
import org.cmdbuild.servlets.json.serializers.translations.commons.AttributeSorter;
import org.cmdbuild.servlets.json.serializers.translations.commons.EntryTypeSorter;
import org.cmdbuild.servlets.json.serializers.translations.commons.TranslationSectionSerializer;
import org.cmdbuild.servlets.json.translationtable.objects.EntryField;
import org.cmdbuild.servlets.json.translationtable.objects.TableEntry;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;

import static org.apache.commons.lang3.StringUtils.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public abstract class EntryTypeTranslationSerializer implements TranslationSectionSerializer {

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
	public abstract Iterable<TranslationSerialization> serialize();

	Iterable<? extends CMAttribute> sortAttributes(final Iterable<? extends CMAttribute> allAttributes) {
		final Iterable<? extends CMAttribute> sortedAttributes = attributeOrdering
				.sortedCopy(nullableIterable(allAttributes));
		return sortedAttributes;
	}

	Collection<TableEntry> serializeAttributes(final Iterable<? extends CMAttribute> attributes) {
		final Collection<TableEntry> attributesSerialization = Lists.newArrayList();
		for (final CMAttribute attribute : nullableIterable(attributes)) {
			final String attributeName = attribute.getName();
			final Collection<EntryField> attributeFields = readFields(attribute);
			final TableEntry jsonAttribute = new TableEntry();
			jsonAttribute.setName(attributeName);
			jsonAttribute.setFields(attributeFields);
			attributesSerialization.add(jsonAttribute);
		}
		return attributesSerialization;
	}

	Collection<EntryField> readFields(final CMClass cmclass) {
		final Collection<EntryField> jsonFields = Lists.newArrayList();
		final TranslationObject translationObject = ClassConverter.DESCRIPTION //
				.withIdentifier(cmclass.getName()) //
				.create();
		final Map<String, String> fieldTranslations = translationLogic.readAll(translationObject);
		final EntryField field = new EntryField();
		field.setName(ClassConverter.description());
		field.setTranslations(fieldTranslations);
		field.setValue(cmclass.getDescription());
		jsonFields.add(field);
		return jsonFields;
	}

	Collection<EntryField> readFields(final CMAttribute attribute) {
		final Collection<EntryField> jsonFields = Lists.newArrayList();
		final String ownerName = attribute.getOwner().getName();
		final TranslationObject translationObjectForDescription = AttributeConverter.CLASSATTRIBUTE_DESCRIPTION //
				.withOwner(ownerName) //
				.withIdentifier(attribute.getName()) //
				.create();
		final Map<String, String> descriptionTranslations = translationLogic.readAll(translationObjectForDescription);
		final EntryField descriptionField = new EntryField();
		descriptionField.setName(AttributeConverter.description());
		descriptionField.setTranslations(descriptionTranslations);
		descriptionField.setValue(attribute.getDescription());
		jsonFields.add(descriptionField);

		if (isNotBlank(attribute.getGroup())) {
			final TranslationObject translationObjectForGroup = AttributeConverter.CLASSATTRIBUTE_GROUP //
					.withOwner(ownerName) //
					.withIdentifier(attribute.getName()) //
					.create();
			final Map<String, String> groupTranslations = translationLogic.readAll(translationObjectForGroup);
			final EntryField groupField = new EntryField();
			groupField.setName(AttributeConverter.group());
			groupField.setTranslations(groupTranslations);
			groupField.setValue(attribute.getGroup());
			jsonFields.add(groupField);
		}
		return jsonFields;
	}

}
