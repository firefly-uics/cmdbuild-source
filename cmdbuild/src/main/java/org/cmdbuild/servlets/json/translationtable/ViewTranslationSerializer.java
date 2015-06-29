package org.cmdbuild.servlets.json.translationtable;

import java.util.Collection;
import java.util.Map;

import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.ViewConverter;
import org.cmdbuild.logic.view.ViewLogic;
import org.cmdbuild.model.view.View;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.json.JSONArray;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class ViewTranslationSerializer implements TranslationSerializer {

	private final ViewLogic viewLogic;
	final TranslationLogic translationLogic;
	Ordering<View> viewOrdering = ViewSorter.DEFAULT.getOrientedOrdering();

	ViewTranslationSerializer(final ViewLogic viewLogic, final TranslationLogic translationLogic,
			final JSONArray sorters) {
		this.viewLogic = viewLogic;
		this.translationLogic = translationLogic;
		setOrderings(sorters);
	}

	private void setOrderings(final JSONArray sorters) {
		// TODO
	}

	@Override
	public JsonResponse serialize() {
		final Iterable<View> views = viewLogic.fetchViewsOfAllTypes();
		final Iterable<View> sortedViews = viewOrdering.sortedCopy(views);
		return serialize(sortedViews);
	}

	JsonResponse serialize(final Iterable<View> sortedViews) {
		final Collection<JsonElement> jsonViews = Lists.newArrayList();
		for (final View view : sortedViews) {
			final String name = view.getName();
			final JsonElement jsonClass = new JsonElement();
			jsonClass.setName(name);
			final Collection<JsonField> classFields = readFields(view);
			jsonClass.setFields(classFields);
			jsonViews.add(jsonClass);
		}
		return JsonResponse.success(jsonViews);
	}

	private Collection<JsonField> readFields(final View view) {
		final Collection<JsonField> jsonFields = Lists.newArrayList();
		final TranslationObject translationObject = ViewConverter.DESCRIPTION //
				.withIdentifier(view.getName()) //
				.create();
		final Map<String, String> fieldTranslations = translationLogic.readAll(translationObject);
		final JsonField field = new JsonField();
		field.setName(ViewConverter.description());
		field.setTranslations(fieldTranslations);
		field.setValue(view.getDescription());
		jsonFields.add(field);
		return jsonFields;
	}

}
