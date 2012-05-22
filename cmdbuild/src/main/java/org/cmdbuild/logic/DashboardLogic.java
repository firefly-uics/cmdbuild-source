package org.cmdbuild.logic;

import static org.cmdbuild.dao.query.clause.FunctionCall.call;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.model.dashboard.ChartDefinition;
import org.cmdbuild.model.dashboard.DashboardDefinition;
import org.cmdbuild.model.dashboard.DashboardDefinition.DashboardColumn;
import org.cmdbuild.services.store.DashboardStore;

/**
 * Business Logic Layer for Data Access
 */
public class DashboardLogic {

	public static final ErrorMessageBuilder errors = new ErrorMessageBuilder();

	private final CMDataView view;
	private final DashboardStore store;

	public DashboardLogic(final CMDataView view, final DashboardStore store) {
		this.view = view;
		this.store = store;
	}

	public Long add(final DashboardDefinition dashboardDefinition) {
		if (dashboardDefinition.getColumns().size() > 0 ||
				dashboardDefinition.getCharts().size() > 0) {
			throw new IllegalArgumentException(errors.initDashboardWithColumns());
		}

		return this.store.add(dashboardDefinition);
	}

	public void modifyBaseProperties(final Long dashboardId, final DashboardDefinition changes) {
		DashboardDefinition dashboard = store.get(dashboardId);

		if (dashboard != null) {
			dashboard.setName(changes.getName());
			dashboard.setDescription(changes.getDescription());
			dashboard.setGroups(changes.getGroups());
	
			store.modify(dashboardId, dashboard);
		} else {
			throw new IllegalArgumentException(errors.undefinedDashboard(dashboardId));
		}
	}

	public void remove(final Long dashboardId) {
		this.store.remove(dashboardId);
	}

	public Map<Long, DashboardDefinition> listDashboards() {
		return this.store.list();
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

	public static class GetChartDataResponse {
		private List<Iterable<Map.Entry<String, Object>>> rows = new ArrayList<Iterable<Map.Entry<String, Object>>>();

		public List<Iterable<Map.Entry<String, Object>>> getRows() {
			return rows;
		}

		private void addRow(Iterable<Map.Entry<String, Object>> x) {
			rows.add(x);
		}
	}

	public String addChart(Long dashboardId, ChartDefinition chartDefinition) {
		DashboardDefinition dashboard = this.store.get(dashboardId);
		String chartId = UUID.randomUUID().toString();
		dashboard.addChart(chartId, chartDefinition);
		this.store.modify(dashboardId, dashboard);
		return chartId;
	}

	public void removeChart(Long dashboardId, String chartId) {
		DashboardDefinition dashboard = this.store.get(dashboardId);
		dashboard.popChart(chartId);
		this.store.modify(dashboardId, dashboard);
	}

	public void moveChart(String chartId, Long fromDashboardId,
			Long toDashboardId) {

		DashboardDefinition to = this.store.get(toDashboardId);
		DashboardDefinition from = this.store.get(fromDashboardId);
		ChartDefinition chart = from.popChart(chartId);

		to.addChart(chartId, chart);

		this.store.modify(toDashboardId, to);
		this.store.modify(fromDashboardId, from);
	}

	public void modifyChart(Long dashboardId, String chartId,
			ChartDefinition chart) {

		DashboardDefinition dashboard = this.store.get(dashboardId);
		dashboard.modifyChart(chartId, chart);

		this.store.modify(dashboardId, dashboard);
	}

	public void setColumns(Long dashboardId, ArrayList<DashboardColumn> columns) {
		DashboardDefinition dashboard = this.store.get(dashboardId);
		dashboard.setColumns(columns);
		this.store.modify(dashboardId, dashboard);
	}

	/* 
	 * to avoid an useless errors hierarchy
	 * define this object that build the errors messages
	 * These are used also in the tests to ensure
	 * that a right message is provided by the exception
	 */
	public static class ErrorMessageBuilder {
		public String initDashboardWithColumns() {
			return "Cannot add a Dashbaord if it has already columns or charts";
		}

		public String undefinedDashboard(Long dashboardId) {
			String errorFormat = "There is no dashboard with id %d";
			return String.format(errorFormat, dashboardId);
		}
 	}
}