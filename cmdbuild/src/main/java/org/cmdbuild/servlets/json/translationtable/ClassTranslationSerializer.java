package org.cmdbuild.servlets.json.translationtable;

import java.util.Collection;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic.AttributesQuery;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

	ClassTranslationSerializer(final DataAccessLogic dataLogic, final boolean activeOnly,
			final TranslationLogic translationLogic, final JSONArray sorters) {
		super(dataLogic, activeOnly, translationLogic);
		setOrderings(sorters);
	}

	private void setOrderings(final JSONArray sorters) {
		if (sorters != null) {
			try {
				for (int i = 0; i < sorters.length(); i++) {
					final JSONObject object = JSONObject.class.cast(sorters.get(i));
					final String element = object.getString(ELEMENT);
					if (element.equalsIgnoreCase(CLASS) || element.equalsIgnoreCase(PROCESS)) {
						entryTypeOrdering = EntryTypeSorter.of(object.getString(FIELD)) //
								.withDirection(object.getString(DIRECTION)) //
								.getOrientedOrdering();
					} else if (element.equalsIgnoreCase(ATTRIBUTE)) {
						attributeOrdering = AttributeSorter.of(object.getString(FIELD)) //
								.withDirection(object.getString(DIRECTION)) //
								.getOrientedOrdering();
					}
				}
			} catch (final JSONException e) {
				//nothing to do
			}
		}
	}
	
	@Override
	public JsonResponse serialize() {
		final Iterable<? extends CMClass> classes = dataLogic.findClasses(activeOnly);
		final Iterable<? extends CMClass> sortedClasses = entryTypeOrdering.sortedCopy(classes);
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

}
