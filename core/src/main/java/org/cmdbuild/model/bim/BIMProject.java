package org.cmdbuild.model.bim;

import org.cmdbuild.data.store.Store.Storable;
import org.joda.time.DateTime;

public class BIMProject implements Storable {

	private String projectId, name, description;
	private boolean active;
	private DateTime lastCheckin;

	@Override
	public String getIdentifier() {
		return getProjectId();
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public DateTime getLastCheckin() {
		return lastCheckin;
	}

	public void setLastCheckin(DateTime lastCheckin) {
		this.lastCheckin = lastCheckin;
	}
}
