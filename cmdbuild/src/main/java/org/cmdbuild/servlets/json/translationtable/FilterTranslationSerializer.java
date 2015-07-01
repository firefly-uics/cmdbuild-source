package org.cmdbuild.servlets.json.translationtable;

import java.util.Collection;
import java.util.Map;

import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.FilterConverter;
import org.cmdbuild.services.store.FilterStore;
import org.cmdbuild.services.store.FilterStore.Filter;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.translationtable.objects.JsonElement;
import org.cmdbuild.servlets.json.translationtable.objects.JsonField;
import org.json.JSONArray;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class FilterTranslationSerializer implements TranslationSerializer {

	private final FilterStore filterStore;
	private final TranslationLogic translationLogic;
	Ordering<Filter> filterOrdering = FilterSorter.DEFAULT.getOrientedOrdering();

	public FilterTranslationSerializer(final FilterStore filterStore, final TranslationLogic translationLogic,
			final JSONArray sorters) {
		this.filterStore = filterStore;
		this.translationLogic = translationLogic;
	}

	@Override
	public JsonResponse serialize() {
		final Iterable<Filter> allFilters = filterStore.fetchAllGroupsFilters();
		final Iterable<Filter> sortedFilters = filterOrdering.sortedCopy(allFilters);
		final Collection<JsonElement> jsonFilters = Lists.newArrayList();
		for (final Filter filter : sortedFilters) {
			final String name = filter.getName();
			final JsonElement jsonFilter = new JsonElement();
			jsonFilter.setName(name);
			final Collection<JsonField> fields = readFields(filter);
			jsonFilter.setFields(fields);
			jsonFilters.add(jsonFilter);
		}
		return JsonResponse.success(jsonFilters);
	}

	private Collection<JsonField> readFields(final Filter filter) {
		final Collection<JsonField> jsonFields = Lists.newArrayList();
		final TranslationObject translationObject = FilterConverter.DESCRIPTION //
				.withIdentifier(filter.getName()) //
				.create();
		final Map<String, String> fieldTranslations = translationLogic.readAll(translationObject);
		final JsonField field = new JsonField();
		field.setName(FilterConverter.description());
		field.setTranslations(fieldTranslations);
		field.setValue(filter.getDescription());
		jsonFields.add(field);
		return jsonFields;
	}

}
