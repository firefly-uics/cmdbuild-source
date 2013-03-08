package org.cmdbuild.servlets.json;

import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.FILTER;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.SOURCE_CLASS_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.SOURCE_FUNCTION;

import java.util.List;

import org.cmdbuild.logic.view.ViewLogic;
import org.cmdbuild.model.View;
import org.cmdbuild.servlets.json.serializers.ViewSerializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONException;
import org.json.JSONObject;

public class ViewManagement extends JSONBase {

/* ************************************************
 * Common
 * ************************************************/

	@JSONExported
	public JSONObject read() throws JSONException {
		return ViewSerializer.toClient(logic().fetchViewsOfAllTypes());
	}

/* ************************************************
 * View from SQL
 * ************************************************/

	@JSONExported
	public void createSQLView(
			@Parameter(value = NAME) final String name, //
			@Parameter(value = DESCRIPTION) final String description, //
			@Parameter(value = SOURCE_FUNCTION) final String sourceFunction //
		) {
		createView(fillSQLView(null, name, description, sourceFunction));
	}

	@JSONExported
	public JSONObject readSQLView() throws JSONException {
		return ViewSerializer.toClient(readByType(View.ViewType.SQL));
	}

	@JSONExported
	public void updateSQLView(
			@Parameter(value = ID) final Long id, //
			@Parameter(value = NAME) final String name, //
			@Parameter(value = DESCRIPTION) final String description, //
			@Parameter(value = SOURCE_FUNCTION) final String sourceFunction //
		) {
		updateView(fillSQLView(id, name, description, sourceFunction));
	}

	@JSONExported
	public void deleteSqlView(
			@Parameter(value = ID) final Long id //
		) {
		deleteViewById(id);
	}

/* ************************************************
 * View from filter
 * ************************************************/

	@JSONExported
	public void createFilterView(
			@Parameter(value = NAME) final String name, //
			@Parameter(value = DESCRIPTION) final String description, //
			@Parameter(value = FILTER) final String filter, //
			@Parameter(value = SOURCE_CLASS_NAME) final String className) { //
		createView(fillFilterView(null, name, description, className, filter));
	}

	@JSONExported
	public JSONObject readFilterView() throws JSONException {
		return ViewSerializer.toClient(readByType(View.ViewType.FILTER));
	}

	@JSONExported
	public void updateFilterView(
			@Parameter(value = NAME) final String name, //
			@Parameter(value = DESCRIPTION) final String description, //
			@Parameter(value = FILTER) final String filter, //
			@Parameter(value = ID) final Long id, //
			@Parameter(value = SOURCE_CLASS_NAME) final String className ) { //

		updateView(fillFilterView(id, name, description, className, filter));
	}

	@JSONExported
	public void deleteFilterView(
			@Parameter(value = ID) final Long id //
		) {

		deleteViewById(id);
	}

/* ************************************************
 * private
 * ************************************************/

	private ViewLogic logic() {
		return new ViewLogic();
	}

	private void createView(final View view) {
		logic().create(view);
	}

	private List<View> readByType(final View.ViewType type) {
		return logic().read(type);
	}

	private void updateView(final View view) {
		logic().update(view);
	}

	private void deleteViewById(final Long id) {
		logic().delete(id);
	}

	private View fillFilterView( //
			final Long id,
			final String name, //
			final String description, //
			final String className, //
			final String filter ) {

		final View view = new View();
		view.setId(id);
		view.setName(name);
		view.setDescription(description);
		view.setSourceClassName(className);
		view.setType(View.ViewType.FILTER);
		view.setFilter(filter);

		return view;
	}

	private View fillSQLView( //
			final Long id,
			final String name, //
			final String description, //
			final String sourceFunction ) {

		final View view = new View();
		view.setId(id);
		view.setName(name);
		view.setDescription(description);
		view.setType(View.ViewType.SQL);
		view.setSourceFunction(sourceFunction);
		return view;
	}
}