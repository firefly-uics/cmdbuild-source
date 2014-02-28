package org.cmdbuild.services.bim;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.service.BimProject;
import org.joda.time.DateTime;

public interface BimFacade {
	
	public interface BimFacadeProject{
		
		String getProjectId();
		
		String getName();
	
		boolean isActive();
		
		boolean isSynch();
	
		String getImportMapping();
	
		String getExportMapping();

		DateTime getLastCheckin();
		
		void setLastCheckin(DateTime lastCheckin);

		File getFile();

	}
	
	BimFacadeProject createProject(BimFacadeProject project);
	
	BimFacadeProject updateProject(BimFacadeProject project);
	
	void disableProject(BimFacadeProject project);
	
	void enableProject(BimFacadeProject project);
	
	List<Entity> readEntityFromProject(EntityDefinition entityDefinition, String projectId);

	String fetchShapeRevision(String shapeName);

	void writeCardIntoProject();

	Iterable<Entity> fetchContainers(String projectId);

	DataHandler download(String projectId);

	
	void createCard(Entity cardData, String targetProjectId, String ifcType, String containerId, String shapeName);
	
	//void createCard(Entity cardData, String targetProjectId, String ifcType, String containerId, String shapeName, String sourceRevisionId);
	
	void removeCard(Entity entity, String projectId, String containerKey);

	String commitTransaction();

	String findShapeWithName(String shapeName, String projectId);

	String roidFromPoid(String poid);

	String fetchGlobalIdFromObjectId(String objectId, String revisionId);

	Map<String, Long> getGlobalidOidMap(String revisionId);

	DataHandler fetchProjectStructure(String revisionId);

	BimProject getProjectByName(String name);
	
	BimProject getProjectById(String projectId);

	void branchFromTo(String projectId, String targetProjectId);

	Iterable<String> fetchAllGlobalIdForIfcType(String string, String identifier);

	Entity fetchEntityFromGlobalId(String revisionId, String globalId);

	String getGlobalidFromOid(String revisionId, Long oid);


}
