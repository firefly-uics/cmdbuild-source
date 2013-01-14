package org.cmdbuild.servlets.json;

import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.services.store.FilterDTO;
import org.cmdbuild.services.store.FilterStore;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONException;
import org.json.JSONObject;

public class Filter extends JSONBase {

	@JSONExported
	public JSONObject read() throws JSONException, CMDBException {
		final FilterStore filterStore = TemporaryObjectsBeforeSpringDI.getFilterStore();
		final Iterable<org.cmdbuild.services.store.FilterStore.Filter> userFilters = filterStore.getAllFilters();

		return new JSONObject();
	}

	@JSONExported
	public JSONObject create(@Parameter(value = "name") final String name, //
			@Parameter(value = "className") final String className, //
			@Parameter(value = "description") final String description, //
			@Parameter(value = "configuration") final JSONObject configuration, //
			@Parameter(value = "groupName", required = false) final String groupName //
	) throws JSONException, CMDBException {

		final FilterStore filterStore = TemporaryObjectsBeforeSpringDI.getFilterStore();
		final FilterDTO filter = FilterDTO.newFilter() //
				.withName(name) //
				.withDescription(description) //
				.withValue(configuration.toString()) //
				.forClass(className) //
				.build();
		filterStore.save(filter);

		final JSONObject out = new JSONObject();
		out.put("name", name);
		out.put("className", className);
		out.put("description", description);
		out.put("configuration", configuration);
		out.put("groupName", groupName);

		return out;
	}

	@JSONExported
	public JSONObject update(@Parameter(value = "name") final String name, //
			@Parameter(value = "className") final String className, //
			@Parameter(value = "description") final String description, //
			@Parameter(value = "configuration") final JSONObject configuration, //
			@Parameter(value = "groupName", required = false) final String groupName //
	) throws JSONException, CMDBException {

		return create(name, className, description, configuration, groupName);
	}

	@JSONExported
	public JSONObject delete(@Parameter(value = "name") final String name, //
			@Parameter(value = "className") final String className //
	) throws JSONException, CMDBException {

		final JSONObject out = new JSONObject();
		out.put("name", name);
		out.put("className", className);

		return out;
	}
}
