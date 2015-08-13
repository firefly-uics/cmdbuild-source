package org.cmdbuild.servlets.json.serializers.translations.csv;

import java.util.Collection;

import org.cmdbuild.logic.translation.SetupFacade;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.services.store.FilterStore;
import org.cmdbuild.services.store.FilterStore.Filter;
import org.cmdbuild.servlets.json.serializers.translations.commons.FilterSorter;
import org.cmdbuild.servlets.json.serializers.translations.commons.TranslationSectionSerializer;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;
import org.json.JSONArray;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class FilterSectionSerializer implements TranslationSectionSerializer {

	private final Iterable<String> enabledLanguages;
	private final TranslationLogic translationLogic;
	private final FilterStore filterStore;
	private final Ordering<Filter> filterOrdering = FilterSorter.DEFAULT.getOrientedOrdering();

	private final Collection<TranslationSerialization> records = Lists.newArrayList();

	public FilterSectionSerializer(final TranslationLogic translationLogic, final JSONArray sorters,
			final SetupFacade setupFacade, final FilterStore filterStore) {
		this.filterStore = filterStore;
		this.translationLogic = translationLogic;
		this.enabledLanguages = setupFacade.getEnabledLanguages();
		// TODO: manage ordering configuration
	}

	@Override
	public Iterable<TranslationSerialization> serialize() {
		final Iterable<Filter> allFilters = filterStore.fetchAllGroupsFilters();
		final Iterable<Filter> sortedFilters = filterOrdering.sortedCopy(allFilters);

		for (final Filter filter : sortedFilters) {
			records.addAll(FilterSerializer.newInstance() //
					.withEnabledLanguages(enabledLanguages) //
					.withTranslationLogic(translationLogic) //
					.withFilterStore(filterStore) //
					.withFilter(filter) //
					.build() //
					.serialize());
		}
		return records;
	}

}
