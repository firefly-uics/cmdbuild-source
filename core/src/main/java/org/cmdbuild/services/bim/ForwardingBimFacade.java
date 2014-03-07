package org.cmdbuild.services.bim;

import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.service.BimProject;

public abstract class ForwardingBimFacade implements BimFacade {

	private final BimFacade delegate;

	public ForwardingBimFacade(final BimFacade delegate) {
		this.delegate = delegate;
	}

	@Override
	public BimFacadeProject createProject(BimFacadeProject project) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BimFacadeProject updateProject(BimFacadeProject project) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void disableProject(BimFacadeProject project) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enableProject(BimFacadeProject project) {
		// TODO Auto-generated method stub

	}

	@Override
	public BimProject getProjectById(String projectId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataHandler download(String projectId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLastRevisionOfProject(String projectId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Entity> readEntityFromProject(EntityDefinition entityDefinition, String projectId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<Entity> fetchEntitiesOfType(String ifcType, String revisionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String createCard(Entity entityToCreate, String targetProjectId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String removeCard(Entity entityToRemove, String targetProjectId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateRelations(Map<String, Map<String, List<String>>> relationsMap, String targetProjectId) {
		// TODO Auto-generated method stub

	}

	@Override
	public String commitTransaction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String findShapeWithName(String shapeName, String revisionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<String> fetchAllGlobalIdForIfcType(String ifcType, String revisionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entity fetchEntityFromGlobalId(String revisionId, String globalId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getContainerOfEntity(String globalId, String sourceRevisionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String fetchGlobalIdFromObjectId(String objectId, String revisionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getGlobalidFromOid(String revisionId, Long oid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataHandler fetchProjectStructure(String revisionId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void updateExportProject(String projectId, String exportProjectId, String shapeProjectId) {
		// TODO Auto-generated method stub
	}

}
