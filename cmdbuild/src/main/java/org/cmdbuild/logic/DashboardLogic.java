package org.cmdbuild.logic;

import static org.cmdbuild.dao.query.clause.FunctionCall.call;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.QueryAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.model.dashboard.ChartDefinition;
import org.cmdbuild.model.dashboard.DashboardDefinition;
import org.cmdbuild.model.dashboard.DashboardDefinition.DashboardColumn;
import org.cmdbuild.services.store.DashboardStore;

/**
 * Business Logic Layer for Dashboards
 */
public class DashboardLogic implements Logic {

	public static final ErrorMessageBuilder errors = new ErrorMessageBuilder();

	private final CMDataView view;
	private final DashboardStore store;
	private final OperationUser operationUser;

	public DashboardLogic(final CMDataView view, final DashboardStore store, OperationUser operationUser) {
		this.view = view;
		this.store = store;
		this.operationUser = operationUser;
	}

	public Long add(final DashboardDefinition dashboardDefinition) {
		if (dashboardDefinition.getColumns().size() > 0 || dashboardDefinition.getCharts().size() > 0) {
			throw new IllegalArgumentException(errors.initDashboardWithColumns());
		}

		return store.create(dashboardDefinition);
	}

	public void modifyBaseProperties(final Long dashboardId, final DashboardDefinition changes) {
		DashboardDefinition dashboard = store.read(dashboardId);

		if (dashboard != null) {
			dashboard.setName(changes.getName());
			dashboard.setDescription(changes.getDescription());
			dashboard.setGroups(changes.getGroups());

			store.update(dashboardId, dashboard);
		} else {
			throw new IllegalArgumentException(errors.undefinedDashboard(dashboardId));
		}
	}

	public void remove(final Long dashboardId) {
		store.delete(dashboardId);
	}

	public Map<Integer, DashboardDefinition> listDashboards() {
		Map<Integer, DashboardDefinition> dashboards = store.list();
		/*
		 * business rule: the admin can show all dashboards, because is the same
		 * behaviour that is implemented for the reports
		 */
		if (operationUser.hasAdministratorPrivileges()) {
			return dashboards;
		}
		Map<Integer, DashboardDefinition> allowedDashboards = new HashMap<Integer, DashboardDefinition>();
		final String currentSelectedGroup = operationUser.getPreferredGroup().getName();
		for (Integer key : dashboards.keySet()) {
			DashboardDefinition dashboardDefinition = dashboards.get(key);
			if (dashboardDefinition.getGroups().contains(currentSelectedGroup)) {
				allowedDashboards.put(key, dashboardDefinition);
			}
		}
		return allowedDashboards;
	}

	public Map<Integer, DashboardDefinition> fullListDashboards() {
		return store.list();
	}

	public Iterable<? extends CMFunction> listDataSources() {
		return view.findAllFunctions();
	}

	public GetChartDataResponse getChartData(final String functionName, final Map<String, Object> params) {
		final CMFunction function = view.findFunctionByName(functionName);
		final NameAlias f = NameAlias.as("f");
		CMQueryResult queryResult = view.select(fakeAnyAttribute(function, f)).from(call(function, params), f).run();
		GetChartDataResponse response = new GetChartDataResponse();
		for (final CMQueryRow row : queryResult) {
			response.addRow(row.getValueSet(f).getValues());
		}
		return response;
	}

	public GetChartDataResponse getChartData(final Long dashboardId, final String chartId,
			final Map<String, Object> params) {

		final DashboardDefinition dashboard = store.read(dashboardId);
		final ChartDefinition chart = dashboard.getChart(chartId);

		return getChartData(chart.getDataSourceName(), params);
	}

	public String addChart(Long dashboardId, ChartDefinition chartDefinition) {
		DashboardDefinition dashboard = store.read(dashboardId);
		String chartId = UUID.randomUUID().toString();
		dashboard.addChart(chartId, chartDefinition);
		// add the chart to the first column if it has some
		// column configured
		List<DashboardColumn> columns = dashboard.getColumns();
		if (columns.size() > 0) {
			columns.get(0).addChart(chartId);
		}

		store.update(dashboardId, dashboard);
		return chartId;
	}

	public void removeChart(Long dashboardId, String chartId) {
		DashboardDefinition dashboard = store.read(dashboardId);
		dashboard.popChart(chartId);
		store.update(dashboardId, dashboard);
	}

	public void moveChart(String chartId, Long fromDashboardId, Long toDashboardId) {

		DashboardDefinition to = store.read(toDashboardId);
		DashboardDefinition from = store.read(fromDashboardId);
		ChartDefinition chart = from.popChart(chartId);

		to.addChart(chartId, chart);

		store.update(toDashboardId, to);
		store.update(fromDashboardId, from);
	}

	public void modifyChart(Long dashboardId, String chartId, ChartDefinition chart) {

		DashboardDefinition dashboard = store.read(dashboardId);
		dashboard.modifyChart(chartId, chart);

		store.update(dashboardId, dashboard);
	}

	public void setColumns(Long dashboardId, ArrayList<DashboardColumn> columns) {
		DashboardDefinition dashboard = store.read(dashboardId);
		dashboard.setColumns(columns);
		store.update(dashboardId, dashboard);
	}

	/*
	 * TODO: Should be replaced by anyAttribute(f) when it works
	 */
	public static Object[] fakeAnyAttribute(CMFunction function, Alias f) {
		List<QueryAttribute> attributes = new ArrayList<QueryAttribute>();
		for (CMFunction.CMFunctionParameter p : function.getOutputParameters()) {
			attributes.add(attribute(f, p.getName()));
		}

		return attributes.toArray(new Object[attributes.size()]);
	}

	private boolean containsAtLeastOneAllowedGroup(Collection<String> alloedGroups, Collection<String> userGroups) {
		for (String userGroup : userGroups) {
			if (alloedGroups.contains(userGroup)) {
				return true;
			}
		}

		return false;
	}

	/*
	 * DTOs
	 */

	public static class GetChartDataResponse {
		private List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		public List<Map<String, Object>> getRows() {
			return rows;
		}

		private void addRow(Iterable<Entry<String, Object>> row) {
			Map<String, Object> dataRow = new HashMap<String, Object>();
			for (Entry<String, Object> entry : row) {
				dataRow.put(entry.getKey(), entry.getValue());
			}

			rows.add(dataRow);
		}
	}

	/*
	 * to avoid an useless errors hierarchy define this object that build the
	 * errors messages These are used also in the tests to ensure that a right
	 * message is provided by the exception
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