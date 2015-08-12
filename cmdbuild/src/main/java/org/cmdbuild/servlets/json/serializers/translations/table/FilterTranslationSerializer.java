package org.cmdbuild.servlets.json.serializers.translations.table;

import java.util.Collection;
import java.util.Map;

import org.cmdbuild.logic.translation.SetupFacade;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.FilterConverter;
import org.cmdbuild.services.store.FilterStore;
import org.cmdbuild.services.store.FilterStore.Filter;
import org.cmdbuild.servlets.json.serializers.translations.commons.FilterSorter;
import org.cmdbuild.servlets.json.serializers.translations.commons.TranslationSerializer;
import org.cmdbuild.servlets.json.translationtable.objects.EntryField;
import org.cmdbuild.servlets.json.translationtable.objects.TableEntry;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;
import org.json.JSONArray;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class FilterTranslationSerializer implements TranslationSerializer {

	private final FilterStore filterStore;
	private final TranslationLogic translationLogic;
	Ordering<Filter> filterOrdering = FilterSorter.DEFAULT.getOrientedOrdering();

	public FilterTranslationSerializer(final FilterStore filterStore, final TranslationLogic translationLogic,
			final JSONArray sorters, final String separator, final SetupFacade setupFacade) {
		this.filterStore = filterStore;
		this.translationLogic = translationLogic;
	}

	@Override
	public Iterable<TranslationSerialization> serialize() {
		final Iterable<Filter> allFilters = filterStore.fetchAllGroupsFilters();
		final Iterable<Filter> sortedFilters = filterOrdering.sortedCopy(allFilters);
		final Collection<TranslationSerialization> jsonFilters = Lists.newArrayList();
		for (final Filter filter : sortedFilters) {
			final String name = filter.getName();
			final TableEntry jsonFilter = new TableEntry();
			jsonFilter.setName(name);
			final Collection<EntryField> fields = readFields(filter);
			jsonFilter.setFields(fields);
			jsonFilters.add(jsonFilter);
		}
		return jsonFilters;
	}

	private Collection<EntryField> readFields(final Filter filter) {
		final Collection<EntryField> jsonFields = Lists.newArrayList();
		final TranslationObject translationObject = FilterConverter.DESCRIPTION //
				.withIdentifier(filter.getName()) //
				.create();
		final Map<String, String> fieldTranslations = translationLogic.readAll(translationObject);
		final EntryField field = new EntryField();
		field.setName(FilterConverter.description());
		field.setTranslations(fieldTranslations);
		field.setValue(filter.getDescription());
		jsonFields.add(field);
		return jsonFields;
	}

}
