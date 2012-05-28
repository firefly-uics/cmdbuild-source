package org.cmdbuild.services.store;

import java.util.HashMap;

import org.cmdbuild.model.dashboard.DashboardDefinition;

public class MapDashboardStore implements DashboardStore {

	private static Long id = new Long(0);
	private static HashMap<Long, DashboardDefinition> dashboards = new HashMap<Long, DashboardDefinition>();

	@Override
	public Long add(DashboardDefinition dashboard) {
		dashboards.put(id, dashboard);
		return id++;
	}

	@Override
	public void modify(Long dashboardId, DashboardDefinition dashboard) {
		dashboards.put(dashboardId, dashboard);
	}

	@Override
	public void remove(Long dashboardId) {
		dashboards.remove(dashboardId);
	}

	@Override
	public DashboardDefinition get(Long dashboardId) {
		return dashboards.get(dashboardId);
	}

	@Override
	public HashMap<Long, DashboardDefinition> list() {
		return dashboards;
	}
}