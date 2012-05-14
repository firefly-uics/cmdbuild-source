package org.cmdbuild.logic;

import static org.cmdbuild.dao.query.clause.FunctionCall.call;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.services.auth.UserContext;

/**
 * Business Logic Layer for Data Access
 */
public class DashboardLogic {

	private final CMDataView view;

	// FIXME Temporary constructor before switching to Spring DI
	public DashboardLogic(final UserContext userCtx) {
		view = TemporaryObjectsBeforeSpringDI.getUserContextView(userCtx);
	}

	public DashboardLogic(final CMDataView view) {
		this.view = view;
	}

	public Iterable<DashboardDefinition> listDashboards() {
		return new ArrayList<DashboardDefinition>();
	}

	public Iterable<? extends CMFunction> listDataSources() {
		return view.findAllFunctions();
	}

	public GetChartDataResponse getChartData(final Long chartId, final Map<String, Object> params) {
		final CMFunction function = view.findFunctionByName("foo");
		final Alias f = Alias.as("f");
		CMQueryResult queryResult = view
			.select(attribute(f, "Bar"), attribute(f, "Baz"))
			.from(call(function, params), f)
			.run();
		GetChartDataResponse response = new GetChartDataResponse();
		for (final CMQueryRow row : queryResult) {
			response.addRow(row.getValueSet(f).getValues());
		}
		return response;
	}

	/*
	 * DTOs
	 */

	public static class DashboardDefinition {
	}

	public static class GetChartDataResponse {
		private List<Iterable<Map.Entry<String, Object>>> rows = new ArrayList<Iterable<Map.Entry<String, Object>>>();

		public List<Iterable<Map.Entry<String, Object>>> getRows() {
			return rows;
		}

		private void addRow(Iterable<Map.Entry<String, Object>> x) {
			rows.add(x);
		}
	}
}
