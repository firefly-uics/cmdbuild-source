package org.cmdbuild.services.store;

import java.util.Map;

import org.cmdbuild.model.dashboard.DashboardDefinition;

public interface DashboardStore {
	public Long add(DashboardDefinition dashboard);
	public void modify(Long dashboardId, DashboardDefinition dashboard);
	public void remove(Long dashboardId);
	public DashboardDefinition get(Long dashboardId);
	public Map<Long, DashboardDefinition> list();
}
