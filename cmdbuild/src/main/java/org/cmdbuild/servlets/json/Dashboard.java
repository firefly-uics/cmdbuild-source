package org.cmdbuild.servlets.json;

import java.io.IOException;
import java.util.Map;

import org.cmdbuild.logic.DashboardLogic;
import org.cmdbuild.logic.DashboardLogic.GetChartDataResponse;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.serializers.JsonDashboardResponses.JsonDashboardListResponse;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class Dashboard extends JSONBase {

	@Admin
	@JSONExported
	public JsonResponse list(
			final UserContext userCtx) {
		final DashboardLogic logic = new DashboardLogic(userCtx);
		JsonDashboardListResponse response = new JsonDashboardListResponse(
				logic.listDashboards(),
				logic.listDataSources()
			);
		return JsonResponse.success(response);
	}

	@JSONExported
	public JsonResponse getChartData(
			@Parameter(value = "chartId") final Long chartId,
			@Parameter(value = "params") final String jsonParams,
			final UserContext userCtx) throws JsonParseException, JsonMappingException, IOException {
		final DashboardLogic logic = new DashboardLogic(userCtx);
		final ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("unchecked") final Map<String, Object> params = mapper.readValue(jsonParams, Map.class);
		GetChartDataResponse result = logic.getChartData(chartId, params);
		return JsonResponse.success(result);
	}

}
