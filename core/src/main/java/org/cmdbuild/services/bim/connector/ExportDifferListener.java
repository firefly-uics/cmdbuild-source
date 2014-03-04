package org.cmdbuild.services.bim.connector;

import java.util.EventListener;

import org.cmdbuild.bim.model.Entity;

public interface ExportDifferListener extends EventListener {
	
	void createTarget(Entity entityToCreate, String targetProjectId);
	
	void deleteTarget(Entity entityToRemove, String targetProjectId);

	void updateRelations(String targetProjectId);

}
