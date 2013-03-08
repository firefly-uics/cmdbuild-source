package org.cmdbuild.servlets.json;

import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.services.store.FilterDTO;
import org.cmdbuild.services.store.FilterStore;
import org.cmdbuild.services.store.FilterStore.GetFiltersResponse;
import org.cmdbuild.servlets.json.serializers.FilterSerializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONException;
import org.json.JSONObject;

import static org.cmdbuild.servlets.json.ComunicationConstants.*;

public class Filter extends JSONBase {

	@JSONExported
	public JSONObject read( //
			@Parameter(value = START) int start, //
			@Parameter(value = LIMIT) int limit //
			) throws JSONException, CMDBException {

		final FilterStore filterStore = TemporaryObjectsBeforeSpringDI.getFilterStore();
		final GetFiltersResponse userFilters = filterStore.getAllFilters(start, limit);

		return FilterSerializer.toClient(userFilters);
	}

	@JSONExported
	public JSONObject readForUser( //
			@Parameter(value = CLASS_NAME) String className //
			) throws JSONException {

		final FilterStore filterStore = TemporaryObjectsBeforeSpringDI.getFilterStore();
		final GetFiltersResponse userFilters = filterStore.getUserFilters(className);

		return FilterSerializer.toClient(userFilters);
	}

	@JSONExported
	public JSONObject create( //
			@Parameter(value = "name") final String name, //
			@Parameter(value = "className") final String className, //
			@Parameter(value = "description") final String description, //
			@Parameter(value = "configuration") final JSONObject configuration, //
			@Parameter(value = "groupName", required = false) final String groupName //
	) throws JSONException, CMDBException {

		final FilterStore filterStore = TemporaryObjectsBeforeSpringDI.getFilterStore();
		final FilterStore.Filter filter = filterStore.save(FilterSerializer.toServer(name, className, description, groupName, configuration));

		return FilterSerializer.toClient(filter, FILTER);
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

		final FilterStore filterStore = TemporaryObjectsBeforeSpringDI.getFilterStore();
		filterStore.delete(FilterDTO.newFilter().withName(name).forClass(className).build());
	}

	@JSONExported
	public JSONObject position( //
			@Parameter(value = ID) final String id //
		) throws JSONException, CMDBException {

		final FilterStore filterStore = TemporaryObjectsBeforeSpringDI.getFilterStore();
		final Long position = filterStore.getPosition(FilterDTO.newFilter().withId(id).build());
		JSONObject out = new JSONObject();
		out.put(POSITION, position);
		return out;
	}
}