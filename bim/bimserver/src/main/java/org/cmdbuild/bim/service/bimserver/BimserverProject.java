package org.cmdbuild.bim.service.bimserver;

import org.bimserver.interfaces.objects.SObjectState;
import org.bimserver.interfaces.objects.SProject;
import org.cmdbuild.bim.service.BimProject;

public class BimserverProject implements BimProject { 

	private static final String ACTIVE = "ACTIVE";
	private final SProject project;

	protected BimserverProject(final SProject project) {
		this.project = project;
	}

	@Override
	public String getIdentifier() {
		final long poid = project.getOid();
		return String.valueOf(poid);
	}

	@Override
	public String getLastRevisionId() {
		final long roid = project.getLastRevisionId();
		return String.valueOf(roid);
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public boolean isActive() {
		final SObjectState state = project.getState();
		return state.name().equals(ACTIVE);
	}
	
	@Override
	public String toString(){
		return project.getOid() + " " + project.getName();
	}

	@Override
	public String getName() {
		return project.getName();
	}

}
