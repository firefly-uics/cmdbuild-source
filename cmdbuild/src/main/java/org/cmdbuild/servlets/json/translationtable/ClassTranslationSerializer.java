package org.cmdbuild.servlets.json.translationtable;

import java.util.Collection;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic.AttributesQuery;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.servlets.json.management.JsonResponse;

import com.google.common.collect.Lists;

public class ClassTranslationSerializer extends EntryTypeTranslationSerializer {

	private static final AttributesQuery NO_LIMIT_AND_OFFSET = new AttributesQuery() {

		@Override
		public Integer limit() {
			return null;
		}

		@Override
		public Integer offset() {
			return null;
		}

	};

	public ClassTranslationSerializer(final DataAccessLogic dataLogic, final boolean activeOnly,
			final TranslationLogic translationLogic) {
		super(dataLogic, activeOnly, translationLogic);
	}

	@Override
	public JsonResponse serialize() {
		final Iterable<? extends CMClass> classes = dataLogic.findClasses(activeOnly);
		final Iterable<? extends CMClass> sortedClasses = EntryTypeSorter //
				.of(ENTRYTYPE_SORTER_PROPERTY) //
				.getOrdering(ENTRYTYPE_SORTER_DIRECTION) //
				.sortedCopy(classes);
		return serialize(sortedClasses);
	}

	JsonResponse serialize(final Iterable<? extends CMClass> sortedClasses) {
		final Collection<JsonElement> jsonClasses = Lists.newArrayList();
		for (final CMClass cmclass : sortedClasses) {
			final String className = cmclass.getName();
			final JsonElement jsonClass = new JsonElement();
			jsonClass.setName(className);
			final Collection<JsonField> classFields = readFields(cmclass);
			final Iterable<? extends CMAttribute> allAttributes = dataLogic.getAttributes(className, activeOnly,
					NO_LIMIT_AND_OFFSET);
			final Iterable<? extends CMAttribute> sortedAttributes = sortAttributes(allAttributes);
			final Collection<JsonElement> jsonAttributes = serializeAttributes(sortedAttributes);
			jsonClass.setAttributes(jsonAttributes);
			jsonClass.setFields(classFields);
			jsonClasses.add(jsonClass);
		}
		return JsonResponse.success(jsonClasses);
	}

	Iterable<? extends CMEntryType> sort(final Iterable<? extends CMEntryType> allAttributes) {
		final Iterable<? extends CMEntryType> sortedAttributes = EntryTypeSorter //
				.of(ENTRYTYPE_SORTER_PROPERTY) //
				.getOrdering(ENTRYTYPE_SORTER_DIRECTION) //
				.sortedCopy(allAttributes);
		return sortedAttributes;
	}

}
