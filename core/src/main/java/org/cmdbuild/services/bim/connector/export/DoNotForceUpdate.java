package org.cmdbuild.services.bim.connector.export;

public abstract class DoNotForceUpdate implements ExportPolicy {

	private final boolean FORCE_UPDATE_YES = false;

	@Override
	public boolean forceUpdate() {
		return FORCE_UPDATE_YES;
	}

}
