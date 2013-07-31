package org.cmdbuild.bim.service.bimserver;

import org.bimserver.interfaces.objects.SObjectState;
import org.bimserver.interfaces.objects.SProject;
import org.cmdbuild.bim.service.BimProject;

public class BimserverProject implements BimProject {

	private final SProject project;

	public BimserverProject(final SProject project) {
		this.project = project;
	}

	@Override
	public String getIdentifier() {
		final Long poid = project.getOid();
		return poid.toString();
	}

	@Override
	public String getName() {
		return project.getName();
	}

	@Override
	public String getLastRevisionId() {
		final Long roid = project.getLastRevisionId();
		return roid.toString();
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public boolean isActive() {
		boolean isActive = false;
		final SObjectState state = project.getState();
		if (state.name().equals("ACTIVE")) {
			isActive = true;
		}
		return isActive;
	}

	@Override
	public boolean hasRevisions() {
		return project.getLastRevisionId() != -1;
	}

}
