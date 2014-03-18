package org.cmdbuild.services.bim.connector.export;

public abstract class ForceUpdate implements ExportPolicy {

	private final boolean FORCE_UPDATE_YES = true;

	@Override
	public boolean forceUpdate() {
		return FORCE_UPDATE_YES;
	}

}
