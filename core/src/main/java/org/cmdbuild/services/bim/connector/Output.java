package org.cmdbuild.services.bim.connector;

import org.cmdbuild.bim.model.Entity;

public interface Output {

	void createTarget(Entity entityToCreate, String targetProjectId);

	void deleteTarget(Entity entityToRemove, String targetProjectId);

	void updateRelations(String targetProjectId);
	
	boolean outputInvalid();

}
