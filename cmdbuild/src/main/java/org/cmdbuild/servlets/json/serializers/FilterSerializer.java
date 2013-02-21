package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.services.store.FilterDTO;
import org.cmdbuild.services.store.FilterStore.Filter;
import org.cmdbuild.services.store.FilterStore.GetFiltersResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FilterSerializer {

	public static JSONObject toClient(final GetFiltersResponse filters) throws JSONException {
		final JSONObject out = new JSONObject();
		final JSONArray jsonFilters = new JSONArray();

		for (final Filter f : filters) {
			jsonFilters.put(toClient(f));
		}

		out.put("filters", jsonFilters);
		out.put("count", filters.count());
		return out;
	}

	public static JSONObject toClient(final Filter filter) throws JSONException {
		return toClient(filter, null);
	}

	public static JSONObject toClient(final Filter filter, final String wrapperName
			) throws JSONException {

		final JSONObject jsonFilter = new JSONObject();
		jsonFilter.put("id", filter.getId());
		jsonFilter.put("name", filter.getName());
		jsonFilter.put("description", filter.getDescription());
		jsonFilter.put("entryType", filter.getClassName());
		jsonFilter.put("configuration", new JSONObject(filter.getValue()));

		JSONObject out = new JSONObject();
		if (wrapperName != null) {
			out.put(wrapperName, jsonFilter);
		} else {
			out = jsonFilter;
		}

		return out;
	}

	public static FilterDTO toServer(final String name, //
			final String className, //
			final String description, //
			final String groupName, //
			final JSONObject configuration) {

		return FilterDTO.newFilter() //
				.withName(name) //
				.withDescription(description) //
				.withValue(configuration.toString()) //
				.forClass(className) //
				.build();
	}
}
