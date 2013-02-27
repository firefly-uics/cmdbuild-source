package org.cmdbuild.servlets.json;

import java.util.List;

import org.cmdbuild.logic.view.ViewLogic;
import org.cmdbuild.model.View;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ViewManagement extends JSONBase {

/* ************************************************
 * View from SQL
 * ************************************************/

	@JSONExported
	public void createSQLView(
			@Parameter(value = PARAMETER_NAME) final String name, //
			@Parameter(value = PARAMETER_DESCRIPTION) final String description, //
			@Parameter(value = PARAMETER_FUNCTION) final String sqlFunctionName //
		) {

	}

	@JSONExported
	public void readSQLView() {

	}

	@JSONExported
	public void updateSQLView(
			@Parameter(value = PARAMETER_ID) final Long id, //
			@Parameter(value = PARAMETER_NAME) final String name, //
			@Parameter(value = PARAMETER_DESCRIPTION) final String description, //
			@Parameter(value = PARAMETER_FUNCTION) final String sqlFunctionName //
			) {

	}

	@JSONExported
	public void deleteSqlView(
			@Parameter(value = PARAMETER_ID) final Long id //
		) {

	}

/* ************************************************
 * View from filter
 * ************************************************/

	@JSONExported
	public void createFilterView(
			@Parameter(value = PARAMETER_NAME) final String name, //
			@Parameter(value = PARAMETER_DESCRIPTION) final String description, //
			@Parameter(value = PARAMETER_FILTER) final String filter, //
			@Parameter(value = PARAMETER_SOURCE_CLASS_NAME) final String className) { //
		final ViewLogic logic = new ViewLogic();
		final View view = fillWidget(null, name, description, className, filter);

		logic.create(view);
	}

	private View fillWidget( //
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

	@JSONExported
	public JSONObject readFilterView() throws JSONException {
		final ViewLogic logic = new ViewLogic();
		final List<View> views = logic.read();
		final JSONObject out = new JSONObject();
		final JSONArray jsonViews = new JSONArray();
		for (View view: views) {
			final JSONObject jsonView = new JSONObject();
			jsonView.put(PARAMETER_DESCRIPTION, view.getDescription());
			jsonView.put(PARAMETER_FILTER, view.getFilter());
			jsonView.put(PARAMETER_ID, view.getId());
			jsonView.put(PARAMETER_NAME, view.getName());
			jsonView.put(PARAMETER_SOURCE_CLASS_NAME, view.getSourceClassName());

			jsonViews.put(jsonView);
		}

		out.put(PARAMETER_VIEWS, jsonViews);
		return out;
	}

	@JSONExported
	public void updateFilterView(
			@Parameter(value = PARAMETER_NAME) final String name, //
			@Parameter(value = PARAMETER_DESCRIPTION) final String description, //
			@Parameter(value = PARAMETER_FILTER) final String filter, //
			@Parameter(value = PARAMETER_ID) final Long id, //
			@Parameter(value = PARAMETER_SOURCE_CLASS_NAME) final String className ) { //

		final ViewLogic logic = new ViewLogic();
		final View view = fillWidget(id, name, description, className, filter);

		logic.update(view);
	}

	@JSONExported
	public void deleteFilterView(
			@Parameter(value = PARAMETER_ID) final Long id //
		) {

		final ViewLogic logic = new ViewLogic();
		logic.delete(id);
	}
}