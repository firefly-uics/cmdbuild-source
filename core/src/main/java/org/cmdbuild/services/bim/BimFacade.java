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

	public interface BimFacadeProject {

		String getProjectId();

		String getName();

		boolean isActive();

		boolean isSynch();

		DateTime getLastCheckin();

		void setLastCheckin(DateTime lastCheckin);

		File getFile();
		
		String getShapeProjectId();
		
		String getExportProjectId();

	}

	// CRUD operations on projects

	BimFacadeProject createProject(BimFacadeProject project);

	BimFacadeProject updateProject(BimFacadeProject project);

	void disableProject(BimFacadeProject project);

	void enableProject(BimFacadeProject project);

	BimProject getProjectById(String projectId);

	DataHandler download(String projectId);

	String getLastRevisionOfProject(String projectId);

	// import-connector

	List<Entity> readEntityFromProject(EntityDefinition entityDefinition, String projectId);

	// export-connector
	
	void updateExportProject(String projectId, String exportProjectId, String shapeProjectId);

	Iterable<Entity> fetchEntitiesOfType(String ifcType, String revisionId);

	String createCard(Entity entityToCreate, String targetProjectId);

	String removeCard(Entity entityToRemove, String targetProjectId);

	void updateRelations(Map<String, Map<String, List<String>>> relationsMap, String targetProjectId);

	void openTransaction(String projectId);

	String commitTransaction();

	void abortTransaction();

	String findShapeWithName(String shapeName, String revisionId);

	Iterable<String> fetchAllGlobalIdForIfcType(String ifcType, String revisionId);

	Entity fetchEntityFromGlobalId(String revisionId, String globalId);

	String getContainerOfEntity(String globalId, String sourceRevisionId);

	// viewer

	String fetchGlobalIdFromObjectId(String objectId, String revisionId);

	String getGlobalidFromOid(String revisionId, Long oid);

	DataHandler fetchProjectStructure(String revisionId);

}
