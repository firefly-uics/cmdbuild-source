package org.cmdbuild.servlets.json.serializers;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.logic.translation.DefaultTranslationLogic.DESCRIPTION_FOR_CLIENT;
import static org.cmdbuild.servlets.json.CommunicationConstants.CONFIGURATION;
import static org.cmdbuild.servlets.json.CommunicationConstants.COUNT;
import static org.cmdbuild.servlets.json.CommunicationConstants.DEFAULT_DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.ENTRY_TYPE;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILTERS;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.TEMPLATE;

import org.cmdbuild.logic.translation.FilterTranslation;
import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.services.store.FilterDTO;
import org.cmdbuild.services.store.FilterStore.Filter;
import org.cmdbuild.services.store.FilterStore.GetFiltersResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FilterSerializer {

	private final TranslationFacade translationFacade;

	public FilterSerializer(final TranslationFacade translationFacade) {
		this.translationFacade = translationFacade;
	}

	public JSONObject toClient(final GetFiltersResponse filters) throws JSONException {
		final JSONObject out = new JSONObject();
		final JSONArray jsonFilters = new JSONArray();

		for (final Filter f : filters) {
			jsonFilters.put(toClient(f));
		}

		out.put(FILTERS, jsonFilters);
		out.put(COUNT, filters.count());
		return out;
	}

	public JSONObject toClient(final Filter filter) throws JSONException {
		return toClient(filter, null);
	}

	public JSONObject toClient(final Filter filter, final String wrapperName) throws JSONException {

		final JSONObject jsonFilter = new JSONObject();
		jsonFilter.put(ID, filter.getId());
		jsonFilter.put(NAME, filter.getName());

		final FilterTranslation translationObject = FilterTranslation.newInstance() //
				.withField(DESCRIPTION_FOR_CLIENT) //
				.withName(filter.getName()) //
				.build();
		final String translatedDescription = translationFacade.read(translationObject);
		jsonFilter.put(DESCRIPTION, defaultIfNull(translatedDescription, filter.getDescription()));
		jsonFilter.put(DEFAULT_DESCRIPTION, filter.getDescription());
		jsonFilter.put(ENTRY_TYPE, filter.getClassName());
		jsonFilter.put(TEMPLATE, filter.isTemplate());
		jsonFilter.put(CONFIGURATION, new JSONObject(filter.getValue()));

		JSONObject out = new JSONObject();
		if (wrapperName != null) {
			out.put(wrapperName, jsonFilter);
		} else {
			out = jsonFilter;
		}

		return out;
	}

	public static FilterDTO toServerForCreation( //
			final String name, //
			final String className, //
			final String description, //
			final JSONObject configuration, //
			final boolean asTemplate) {

		return FilterDTO.newFilter() //
				.withName(name) //
				.withDescription(description) //
				.withValue(configuration.toString()) //
				.forClass(className) //
				.asTemplate(asTemplate) //
				.build();
	}

	public static FilterDTO toServerForUpdate( //
			final Long id, //
			final String name, //
			final String className, //
			final String description, //
			final JSONObject configuration) {

		return FilterDTO.newFilter() //
				.withId(id) //
				.withName(name) //
				.withDescription(description) //
				.withValue(configuration.toString()) //
				.forClass(className) //
				.build();
	}

}
