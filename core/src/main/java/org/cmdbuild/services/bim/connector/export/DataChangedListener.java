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
	public void outputInvalid(final String outputId) {
		System.out.println("I don't know if I have to do something...");
	}

	@Override
	public void createTarget(final Entity entityToCreate, final String targetProjectId) {
		throw new DataChangedException();
	}

	@Override
	public void deleteTarget(final Entity entityToRemove, final String targetProjectId) {
		throw new DataChangedException();
	}

	@Override
	public void updateRelations(final String targetProjectId) {
		throw new DataNotChangedException();
	}

}