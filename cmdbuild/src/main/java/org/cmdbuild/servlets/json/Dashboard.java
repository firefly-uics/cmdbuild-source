package org.cmdbuild.servlets.json;

import org.cmdbuild.logic.DashboardLogic;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.serializers.JsonDashboardResponses.JsonDashboardListResponse;

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

}
