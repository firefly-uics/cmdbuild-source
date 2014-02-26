package org.cmdbuild.services.bim;

import static org.cmdbuild.bim.utils.BimConstants.DEFAULT_TAG_EXPORT;
import static org.cmdbuild.bim.utils.BimConstants.GLOBALID_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.IFC_AXIS2_PLACEMENT3D;
import static org.cmdbuild.bim.utils.BimConstants.IFC_CARTESIAN_POINT;
import static org.cmdbuild.bim.utils.BimConstants.IFC_COORDINATES;
import static org.cmdbuild.bim.utils.BimConstants.IFC_DESCRIPTION;
import static org.cmdbuild.bim.utils.BimConstants.IFC_GLOBALID;
import static org.cmdbuild.bim.utils.BimConstants.IFC_LOCATION;
import static org.cmdbuild.bim.utils.BimConstants.IFC_NAME;
import static org.cmdbuild.bim.utils.BimConstants.IFC_OBJECT_TYPE;
import static org.cmdbuild.bim.utils.BimConstants.IFC_RELATED_ELEMENTS;
import static org.cmdbuild.bim.utils.BimConstants.IFC_RELATING_STRUCTURE;
import static org.cmdbuild.bim.utils.BimConstants.IFC_RELATIVE_PLACEMENT;
import static org.cmdbuild.bim.utils.BimConstants.IFC_REL_CONTAINED;
import static org.cmdbuild.bim.utils.BimConstants.IFC_TAG;
import static org.cmdbuild.common.Constants.BASE_CLASS_NAME;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.cmdbuild.bim.mapper.Reader;
import org.cmdbuild.bim.mapper.xml.BimReader;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimRevision;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.bim.service.bimserver.BimserverEntity;
import org.cmdbuild.bim.service.bimserver.BimserverReferenceAttribute;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DefaultBimFacade implements BimFacade {

	private final Map<String, String> container_relation_Map = Maps.newHashMap();

	private static final String IFC_SPACE = "IfcSpace";

	private static final String NULL_TRANSACTION_ID = "-1";

	private static final String NULL_REVISION_ID = "-1";
	private final BimService service;
	private final Reader reader;

	private String transactionId = NULL_TRANSACTION_ID;

	public DefaultBimFacade(BimService bimservice) {
		this.service = bimservice;
		reader = new BimReader(bimservice);
	}

	@Override
	public BimFacadeProject createProject(BimFacadeProject project) {
		final BimProject createdProject = service.createProject(project.getName());
		final String projectId = createdProject.getIdentifier();
		if (project.getFile() != null) {
			DateTime lastCheckin = service.checkin(createdProject.getIdentifier(), project.getFile());
			final BimProject updatedProject = service.getProjectByPoid(projectId);
			createdProject.setLastCheckin(lastCheckin);
			final BimRevision lastRevision = service.getRevision(updatedProject.getLastRevisionId());
			if (lastRevision == null) {
				throw new BimError("Upload failed");
			}
		}
		final BimFacadeProject facadeProject = from(createdProject);
		return facadeProject;
	}

	@Override
	public BimFacadeProject updateProject(BimFacadeProject project) {
		final String projectId = project.getProjectId();
		BimProject bimProject = service.getProjectByPoid(projectId);
		if (project.getFile() != null) {
			DateTime checkin = service.checkin(projectId, project.getFile());
			bimProject = service.getProjectByPoid(projectId);
			bimProject.setLastCheckin(checkin);
		}
		if (project.isActive() != bimProject.isActive()) {
			if (project.isActive()) {
				service.enableProject(projectId);
			} else {
				service.disableProject(projectId);
			}
		}
		final BimFacadeProject facadeProject = from(bimProject);
		return facadeProject;
	}

	private static BimFacadeProject from(final BimProject createdProject) {
		BimFacadeProject project = new BimFacadeProject() {

			@Override
			public boolean isSynch() {
				return false;
			}

			@Override
			public boolean isActive() {
				return createdProject.isActive();
			}

			@Override
			public String getProjectId() {
				return createdProject.getIdentifier();
			}

			@Override
			public String getName() {
				return createdProject.getName();
			}

			@Override
			public DateTime getLastCheckin() {
				return createdProject.getLastCheckin();
			}

			@Override
			public String getImportMapping() {
				throw new UnsupportedOperationException();
			}

			@Override
			public File getFile() {
				throw new UnsupportedOperationException();
			}

			@Override
			public String getExportMapping() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void setLastCheckin(DateTime lastCheckin) {
				throw new UnsupportedOperationException();
			}
		};
		return project;

	}

	@Override
	public void disableProject(BimFacadeProject project) {
		login();
		service.disableProject(project.getProjectId());
		logout();

	}

	@Override
	public void enableProject(BimFacadeProject project) {
		login();
		service.enableProject(project.getProjectId());
		logout();
	}

	@Override
	public DataHandler download(String projectId) {
		String revisionId = service.getProjectByPoid(projectId).getLastRevisionId();
		if (("-1").equals(revisionId)) {
			return null;
		}
		return service.downloadIfc(revisionId);
	}

	@Override
	public Iterable<Entity> fetchContainers(String projectId) {
		String revisionId = service.getProjectByPoid(projectId).getLastRevisionId();
		Iterable<Entity> entities = service.getEntitiesByType(revisionId, IFC_SPACE);
		return entities;
	}

	@Override
	public String fetchShapeRevision(String shapeName) {
		throw new RuntimeException("fetchShapeRevision not implemented");
	}

	@Override
	public List<Entity> readEntityFromProject(EntityDefinition entityDefinition, String projectId) {
		login();
		BimProject project = service.getProjectByPoid(projectId);
		String revisionId = project.getLastRevisionId();
		List<Entity> source = reader.readEntities(revisionId, entityDefinition);
		logout();
		return source;
	}

	@Deprecated
	public BimService service() {
		return service;
	}

	@Override
	public void writeCardIntoProject() {
		throw new RuntimeException("writeCardIntoProject not implemented");
	}

	private void login() {
	}

	private void logout() {
	}

	@Override
	public void createCard(Entity cardData, String projectId, String ifcType, String containerKey, String shapeOid) {

		System.out.println(projectId);

		if (transactionId.equals(NULL_TRANSACTION_ID)) {
			transactionId = service.openTransaction(projectId);
			System.out.println("Transaction " + transactionId + " opened at " + new DateTime());
		}

		System.out.println("Insert card " + cardData.getAttributeByName(ID_ATTRIBUTE).getValue());

		System.out.println("IfcType " + ifcType);
		System.out.println("BASE_CLASS_NAME " + cardData.getAttributeByName(BASE_CLASS_NAME).getValue());
		System.out.println("GLOBALID_ATTRIBUTE " + cardData.getAttributeByName(GLOBALID_ATTRIBUTE).getValue());
		System.out.println("CODE_ATTRIBUTE " + cardData.getAttributeByName(CODE_ATTRIBUTE).getValue());
		System.out.println("DESCRIPTION_ATTRIBUTE " + cardData.getAttributeByName(DESCRIPTION_ATTRIBUTE).getValue());
		System.out.println("DEFAULT_TAG_EXPORT " + DEFAULT_TAG_EXPORT);

		String objectOid = service.createObject(transactionId, ifcType);
		service.setStringAttribute(transactionId, objectOid, IFC_OBJECT_TYPE,
				cardData.getAttributeByName(BASE_CLASS_NAME).getValue());
		service.setStringAttribute(transactionId, objectOid, IFC_GLOBALID,
				cardData.getAttributeByName(GLOBALID_ATTRIBUTE).getValue());
		service.setStringAttribute(transactionId, objectOid, IFC_NAME, cardData.getAttributeByName(CODE_ATTRIBUTE)
				.getValue());
		service.setStringAttribute(transactionId, objectOid, IFC_DESCRIPTION,
				cardData.getAttributeByName(DESCRIPTION_ATTRIBUTE).getValue());
		service.setStringAttribute(transactionId, objectOid, IFC_TAG, DEFAULT_TAG_EXPORT);

		// write coordinates
		// String placementOid = service.createObject(transactionId,
		// IFC_LOCAL_PLACEMENT);
		// service.setReference(transactionId, objectOid, IFC_OBJECT_PLACEMENT,
		// placementOid);
		// setCoordinates(placementOid, bimData.get(X_COORD),
		// bimData.get(Y_COORD), bimData.get(Z_COORD), transactionId);
		// setRelationWithContainer(objectOid, containerKey, placementOid,
		// revisionId, transactionId);
		//
		// service.setReference(transactionId, objectOid, "Representation",
		// shapeOid);
	}

	@Override
	public void removeCard(Entity entity, String projectId, String containerKey) {

		if (transactionId.equals(NULL_TRANSACTION_ID)) {
			transactionId = service.openTransaction(projectId);
			System.out.println("Transaction " + transactionId + " opened at " + new DateTime());
		}
		System.out.println("Delete card " + entity + "...");
		final String revisionId = service.getProjectByPoid(projectId).getLastRevisionId();
		service.removeObject(transactionId, revisionId, entity.getKey());

		// TODO
		// remove relation with container
	}

	public String findShapeWithName(String shapeName, String projectId) {
		String revisionId = service.getProjectByPoid(projectId).getLastRevisionId();
		Iterable<Entity> shapeList = service.getEntitiesByType(revisionId, "IfcProductDefinitionShape");
		for (Entity shape : shapeList) {
			Attribute shapeNameAttribute = shape.getAttributeByName("Name");
			if (shapeNameAttribute.getValue() != null && shapeNameAttribute.getValue().equals(shapeName)) {
				System.out.println("Shape found with id " + shape.getKey());
				return shape.getKey();
			}
		}
		return "-1";
	}

	@Override
	public String commitTransaction() {
		String revisionId = NULL_REVISION_ID;
		if (transactionId.equals(NULL_TRANSACTION_ID)) {
			return NULL_REVISION_ID;
		}
		try {
			revisionId = service.commitTransaction(transactionId);
			System.out.println("Transaction " + transactionId + " committed at " + new DateTime());
		} finally {
			// FIXME transactionId is not reset if the commit fails!!!!
			transactionId = NULL_TRANSACTION_ID;
		}
		return revisionId;
	}

	private void setCoordinates(String placementId, String x1, String x2, String x3, String transactionId) {
		double x1d = Double.parseDouble(x1);
		double x2d = Double.parseDouble(x2);
		double x3d = Double.parseDouble(x3);

		String relativePlacementId = service.createObject(transactionId, IFC_AXIS2_PLACEMENT3D);
		service.setReference(transactionId, placementId, IFC_RELATIVE_PLACEMENT, relativePlacementId);
		String cartesianPointId = service.createObject(transactionId, IFC_CARTESIAN_POINT);
		System.out.println("Set coordinates " + x1d + " " + x2d + " " + x3d);
		service.addDoubleAttribute(transactionId, cartesianPointId, IFC_COORDINATES, x1d);
		service.addDoubleAttribute(transactionId, cartesianPointId, IFC_COORDINATES, x2d);
		service.addDoubleAttribute(transactionId, cartesianPointId, IFC_COORDINATES, x3d);
		service.setReference(transactionId, relativePlacementId, IFC_LOCATION, cartesianPointId);
	}

	private void setRelationWithContainer(String objectId, String containerKey, String placementId, String revisionId,
			String transactionId) {

		Entity container = service.getEntityByGuid(revisionId, containerKey);
		String containerOid = (BimserverEntity.class.cast(container)).getOid().toString();
		String relationOid = "";
		if (container_relation_Map.containsKey(containerOid)) {
			relationOid = container_relation_Map.get(containerOid).toString();
		} else {
			Iterable<Entity> relContained = service.getEntitiesByType(revisionId, IFC_REL_CONTAINED);
			for (Iterator<Entity> it1 = relContained.iterator(); it1.hasNext();) {
				BimserverEntity rel = (BimserverEntity) it1.next();
				if (((BimserverReferenceAttribute) rel.getAttributeByName(IFC_RELATING_STRUCTURE)).getGlobalId()
						.equals(container.getKey())) {
					relationOid = rel.getOid().toString();
					container_relation_Map.put(containerOid, relationOid);
					break;
				}
			}
		}
		if (!relationOid.isEmpty()) {
			System.out.println("Relation found -> insert object");
			service.addReference(transactionId, relationOid, IFC_RELATED_ELEMENTS, objectId);
		} else {
			System.out.println("Relation not found -> create relation");
			relationOid = service.createObject(transactionId, IFC_REL_CONTAINED);
			container_relation_Map.put(containerOid, relationOid);
			service.setReference(transactionId, relationOid, IFC_RELATING_STRUCTURE, containerOid);
			service.addReference(transactionId, relationOid, IFC_RELATED_ELEMENTS, objectId);
		}
	}

	@Override
	public String roidFromPoid(String poid) {
		final BimProject project = service.getProjectByPoid(poid);
		return project.getLastRevisionId();
	}

	@Override
	public String fetchGlobalIdFromObjectId(final String objectId, final String revisionId) {
		Entity entity = service.getEntityByOid(revisionId, objectId);
		return entity.getGlobalId();
	}

	@Override
	public Map<Long, String> fetchAllGlobalId(String revisionId) {
		Map<Long, String> globalIdMap = service.getAllGloabalId(revisionId);
		return globalIdMap;
	}

	@Override
	public DataHandler fetchProjectStructure(String revisionId) {
		return service.fetchProjectStructure(revisionId);

	}

	@Override
	public BimProject getProjectByName(String name) {
		return service.getProjectByName(name);
	}

	@Override
	public BimProject getProjectById(String projectId) {
		return service.getProjectByPoid(projectId);
	}

	@Override
	public void branchFromTo(String sourceProjectId, String targetProjectId) {
		BimProject project = service.getProjectByPoid(sourceProjectId);
		service.branchToExistingProject(project.getLastRevisionId(), targetProjectId);

	}

	@Override
	public BimProject fetchCorrespondingProjectForExport(String sourceProjectId) {
		final BimProject sourceProject = service.getProjectByPoid(sourceProjectId);
		final String projectName = sourceProject.getName();
		final String revisionId = sourceProject.getLastRevisionId();
		final BimProject projectForExport = service.getProjectByName(projectName + "_export_" + revisionId);
		return projectForExport;
	}

	@Override
	public Iterable<String> fetchAllGlobalIdForIfcType(String ifcType, String projectId) {
		List<String> globalIdList = Lists.newArrayList();
		String revisionId = service.getProjectByPoid(projectId).getLastRevisionId();
		Iterable<Entity> entities = service.getEntitiesByType(revisionId, ifcType);
		for (Entity entity : entities) {
			globalIdList.add(entity.getGlobalId());
		}
		return globalIdList;
	}

	@Override
	public Entity fetchEntityFromGlobalId(String revisionId, String globalId) {
		Entity entity = Entity.NULL_ENTITY;
		try {
			entity = service.getEntityByGuid(revisionId, globalId);
		} catch (Exception e) {
		}
		return entity;
	}

}
