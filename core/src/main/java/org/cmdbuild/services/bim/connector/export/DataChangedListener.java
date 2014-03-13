package org.cmdbuild.services.bim.connector.export;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.services.bim.connector.Output;

public class DataChangedListener implements Output {

	@SuppressWarnings("serial")
	public static class DataChangedException extends RuntimeException {
	}
	
	@SuppressWarnings("serial")
	public static class DataNotChangedException extends RuntimeException {
	}
	
	@SuppressWarnings("serial")
	public static class InvalidOutputException extends RuntimeException {
	}

	@Override
	public boolean outputInvalid() {
		throw new InvalidOutputException();
	}

	@Override
	public void createTarget(Entity entityToCreate, String targetProjectId) {
		throw new DataChangedException();
	}

	@Override
	public void deleteTarget(Entity entityToRemove, String targetProjectId) {
		throw new DataChangedException();
	}

	@Override
	public void updateRelations(String targetProjectId) {
		throw new DataNotChangedException();
	}

}