package org.cmdbuild.services.bim.connector;

import java.util.EventListener;

import org.cmdbuild.bim.model.Entity;

public interface ExportDifferListener extends EventListener {
	
	void createTarget(Entity source, String targetProjectId, String typeName, String containerKey, String shapeOid, String sourceRevisionId);
	
	void deleteTarget(Entity cardData, String targetProjectId, String containerKey);
}
