package org.cmdbuild.servlets.json;

import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.services.store.FilterStore;
import org.cmdbuild.servlets.json.serializers.FilterSerializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONException;
import org.json.JSONObject;

public class Filter extends JSONBase {

	@JSONExported
	public JSONObject read() throws JSONException, CMDBException {
		final FilterStore filterStore = TemporaryObjectsBeforeSpringDI.getFilterStore();
		final Iterable<org.cmdbuild.services.store.FilterStore.Filter> userFilters = filterStore.getAllFilters();

		return FilterSerializer.toClient(userFilters);
	}

	@JSONExported
	public void create(@Parameter(value = "name") final String name, //
			@Parameter(value = "className") final String className, //
			@Parameter(value = "description") final String description, //
			@Parameter(value = "configuration") final JSONObject configuration, //
			@Parameter(value = "groupName", required = false) final String groupName //
	) throws JSONException, CMDBException {

		final FilterStore filterStore = TemporaryObjectsBeforeSpringDI.getFilterStore();
		filterStore.save(FilterSerializer.toServer(name, className, description, groupName, configuration));
	}

	@JSONExported
	public void update(@Parameter(value = "name") final String name, //
			@Parameter(value = "className") final String className, //
			@Parameter(value = "description") final String description, //
			@Parameter(value = "configuration") final JSONObject configuration, //
			@Parameter(value = "groupName", required = false) final String groupName //
	) throws JSONException, CMDBException {

		create(name, className, description, configuration, groupName);
	}

	@JSONExported
	public void delete(@Parameter(value = "name") final String name, //
			@Parameter(value = "className") final String className //
	) throws JSONException, CMDBException {

		// TODO delete
	}
}
