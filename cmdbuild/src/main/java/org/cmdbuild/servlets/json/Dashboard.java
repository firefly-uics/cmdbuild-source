package org.cmdbuild.servlets.json;

import java.io.IOException;
import java.util.Map;

import org.cmdbuild.logic.DashboardLogic;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.DashboardLogic.GetChartDataResponse;
import org.cmdbuild.model.dashboard.ChartDefinition;
import org.cmdbuild.model.dashboard.DashboardDefinition;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.serializers.JsonDashboardDTO.JsonDashboardListResponse;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

public class Dashboard extends JSONBase {

	private static ObjectMapper mapper;

	public Dashboard() {
		super();
		mapper = new ObjectMapper();

		mapper.setSerializationConfig(mapper.copySerializationConfig()
			.withSerializationInclusion(JsonSerialize.Inclusion.NON_NULL) // to exclude null values
			.withSerializationInclusion(JsonSerialize.Inclusion.NON_EMPTY) // to exclude empty map or array
		);
	}

	@Admin
	@JSONExported
	public JsonResponse list(
			final UserContext userCtx) {
		final DashboardLogic logic = TemporaryObjectsBeforeSpringDI.getDashboardLogic(userCtx);
		JsonDashboardListResponse response = new JsonDashboardListResponse(
				logic.listDashboards(),
				logic.listDataSources()
			);
		return JsonResponse.success(response);
	}

	@Admin
	@JSONExported
	public JsonResponse add(
			final UserContext userCtx,
			@Parameter(value = "dashboardConfiguration") final String jsonDashboard) throws Exception {

		final DashboardLogic logic = TemporaryObjectsBeforeSpringDI.getDashboardLogic(userCtx);
		final DashboardDefinition dashboard = mapper.readValue(jsonDashboard, DashboardDefinition.class);

		Long dashboardId = logic.add(dashboard);

		return JsonResponse.success(dashboardId);
	}

	@Admin
	@JSONExported
	public void modifyBaseProperties(
			final UserContext userCtx,
			@Parameter(value = "dashboardId") final Long dashboardId,
			@Parameter(value = "dashboardConfiguration") final String jsonDashboard) throws Exception{

		final DashboardLogic logic = TemporaryObjectsBeforeSpringDI.getDashboardLogic(userCtx);
		final DashboardDefinition dashboard = mapper.readValue(jsonDashboard, DashboardDefinition.class);

		logic.modifyBaseProperties(dashboardId, dashboard);
	}

	@Admin
	@JSONExported
	public void remove(
			final UserContext userCtx,
			@Parameter(value = "dashboardId") final Long dashboardId) {

		final DashboardLogic logic = TemporaryObjectsBeforeSpringDI.getDashboardLogic(userCtx);

		logic.remove(dashboardId);
	}

	@Admin
	@JSONExported
	public JsonResponse addChart(
			final UserContext userCtx,
			@Parameter(value = "dashboardId") final Long dashboardId,
			@Parameter(value = "chartConfiguration") final String jsonChartConfiguration) throws Exception {

		final DashboardLogic logic = TemporaryObjectsBeforeSpringDI.getDashboardLogic(userCtx);
		final ChartDefinition chartDefinition = mapper.readValue(jsonChartConfiguration, ChartDefinition.class);

		return JsonResponse.success(logic.addChart(dashboardId, chartDefinition));
	}

	@Admin
	@JSONExported
	public void modifyChart(
			final UserContext userCtx,
			@Parameter(value = "dashboardId") final Long dashboardId,
			@Parameter(value = "chartId") final String chartId,
			@Parameter(value = "chartConfiguration") final String jsonChartConfiguration) throws Exception {

		final DashboardLogic logic = TemporaryObjectsBeforeSpringDI.getDashboardLogic(userCtx);
		final ChartDefinition chartDefinition = mapper.readValue(jsonChartConfiguration, ChartDefinition.class);

		logic.modifyChart(dashboardId, chartId, chartDefinition);
	}

	@Admin
	@JSONExported
	public void removeChart(
			final UserContext userCtx,
			@Parameter(value = "dashboardId") final Long dashboardId,
			@Parameter(value = "chartId") final String chartId) {

		final DashboardLogic logic = TemporaryObjectsBeforeSpringDI.getDashboardLogic(userCtx);

		logic.removeChart(dashboardId, chartId);
	}

	@Admin
	@JSONExported
	public void moveChart(
			final UserContext userCtx,
			@Parameter(value = "chartId") final String chartId,
			@Parameter(value = "fromDashboardId") final Long fromDashboardId,
			@Parameter(value = "toDashboardId") final Long toDashboardId) {

		final DashboardLogic logic = TemporaryObjectsBeforeSpringDI.getDashboardLogic(userCtx);
		logic.moveChart(chartId, fromDashboardId, toDashboardId);
	}

	@JSONExported
	public JsonResponse getChartData(
			@Parameter(value = "chartId") final Long chartId,
			@Parameter(value = "params") final String jsonParams,
			final UserContext userCtx) throws JsonParseException, JsonMappingException, IOException {

		final DashboardLogic logic = TemporaryObjectsBeforeSpringDI.getDashboardLogic(userCtx);
		final ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("unchecked") final Map<String, Object> params = mapper.readValue(jsonParams, Map.class);
		GetChartDataResponse result = logic.getChartData(chartId, params);
		return JsonResponse.success(result);
	}
}