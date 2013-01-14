package org.cmdbuild.servlets.json.serializers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.cmdbuild.services.store.FilterDTO;
import org.cmdbuild.services.store.FilterStore.Filter;

public class FilterSerializer {
	public static JSONObject toClient(Iterable<Filter> filters) throws JSONException {
		JSONObject out = new JSONObject();
		JSONArray jsonFilters = new JSONArray();

		for (Filter f: filters) {
			jsonFilters.put(toClient(f));
		}

		out.put("filters", jsonFilters);
		return out;
	}

	public static JSONObject toClient(Filter filter) throws JSONException {
		JSONObject jsonFilter = new JSONObject();
		jsonFilter.put("name", filter.getName());
		jsonFilter.put("description", filter.getDescription());
		jsonFilter.put("entryType", filter.getClassName());
		jsonFilter.put("configuration", new JSONObject(filter.getValue()));

		return jsonFilter;
	}

	public static FilterDTO toServer( 
			String  name, //
			String className, //
			String description, //
			String groupName, //
			JSONObject configuration) {

		return FilterDTO.newFilter() //
				.withName(name) //
				.withDescription(description) //
				.withValue(configuration.toString()) //
				.forClass(className) //
				.build();
	}
}
