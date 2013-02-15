package org.cmdbuild.servlets.json;

import org.cmdbuild.servlets.utils.Parameter;
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
			@Parameter(value = PARAMETER_NAME) final String name, //
			@Parameter(value = PARAMETER_DESCRIPTION) final String description, //
			@Parameter(value = PARAMETER_FUNCTION) final String sqlFunctionName //
			) {

	}

	@JSONExported
	public void deleteSqlView(
			@Parameter(value = PARAMETER_NAME) final String name //
		) {

	}

/* ************************************************
 * View from filter
 * ************************************************/

	@JSONExported
	public void createFilterView(
			@Parameter(value = PARAMETER_NAME) final String name, //
			@Parameter(value = PARAMETER_DESCRIPTION) final String description, //
			@Parameter(value = PARAMETER_CLASS_NAME) final String className, //
			@Parameter(value = PARAMETER_FILTER) final JSONObject filter //	
		) {

	}

	@JSONExported
	public void readFilterView() {

	}

	@JSONExported
	public void updateFilterView(
			@Parameter(value = PARAMETER_NAME) final String name, //
			@Parameter(value = PARAMETER_DESCRIPTION) final String description, //
			@Parameter(value = PARAMETER_FILTER) final JSONObject filter //		
		) {

	}

	@JSONExported
	public void deleteFilterView(
			@Parameter(value = PARAMETER_NAME) final String name //
		) {

	}
}