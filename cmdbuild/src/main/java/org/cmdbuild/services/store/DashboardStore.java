package org.cmdbuild.services.store;

import java.util.HashMap;

import org.cmdbuild.model.dashboard.DashboardDefinition;

public interface DashboardStore {
	public Long add(DashboardDefinition dashboard);
	public void modify(Long ddId, DashboardDefinition dd);
	public void remove(Long ddId);
	public DashboardDefinition get(Long ddId);
	public HashMap<Long, DashboardDefinition> list();
}
