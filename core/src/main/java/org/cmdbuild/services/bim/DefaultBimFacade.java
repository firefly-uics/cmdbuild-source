package org.cmdbuild.services.bim;

import static org.cmdbuild.bim.utils.BimConstants.DEFAULT_TAG_EXPORT;
import static org.cmdbuild.bim.utils.BimConstants.GLOBALID_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.IFC_AXIS2_PLACEMENT3D;
import static org.cmdbuild.bim.utils.BimConstants.IFC_CARTESIAN_POINT;
import static org.cmdbuild.bim.utils.BimConstants.IFC_COORDINATES;
import static org.cmdbuild.bim.utils.BimConstants.IFC_DESCRIPTION;
import static org.cmdbuild.bim.utils.BimConstants.IFC_GLOBALID;
import static org.cmdbuild.bim.utils.BimConstants.IFC_LOCAL_PLACEMENT;
import static org.cmdbuild.bim.utils.BimConstants.IFC_LOCATION;
import static org.cmdbuild.bim.utils.BimConstants.IFC_NAME;
import static org.cmdbuild.bim.utils.BimConstants.IFC_OBJECT_PLACEMENT;
import static org.cmdbuild.bim.utils.BimConstants.IFC_OBJECT_TYPE;
import static org.cmdbuild.bim.utils.BimConstants.IFC_RELATED_ELEMENTS;
import static org.cmdbuild.bim.utils.BimConstants.IFC_RELATING_STRUCTURE;
import static org.cmdbuild.bim.utils.BimConstants.IFC_RELATIVE_PLACEMENT;
import static org.cmdbuild.bim.utils.BimConstants.IFC_REL_CONTAINED;
import static org.cmdbuild.bim.utils.BimConstants.IFC_TAG;
import static org.cmdbuild.bim.utils.BimConstants.IFC_TYPE;
import static org.cmdbuild.bim.utils.BimConstants.OBJECT_OID;
import static org.cmdbuild.bim.utils.BimConstants.X_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.Y_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.Z_ATTRIBUTE;
import static org.cmdbuild.common.Constants.BASE_CLASS_NAME;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;
import static org.cmdbuild.services.bim.connector.DefaultBimDataView.SHAPE_OID;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataHandler;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.bim.mapper.Reader;
import org.cmdbuild.bim.mapper.xml.BimReader;
import org.cmdbuild.bim.model.Attribute;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimRevision;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.bim.service.ListAttribute;
import org.cmdbuild.bim.service.ReferenceAttribute;
import org.cmdbuild.bim.service.bimserver.BimserverEntity;
import org.joda.time.DateTime;

import com.google.common.collect.Lists;

public class DefaultBimFacade implements BimFacade {

	private final BimService service;
	private final TransactionManager transactionManager;
	private final Reader reader;
	private String transactionId;

	public DefaultBimFacade(BimService bimservice, TransactionManager transactionManager) {
		this.service = bimservice;
		this.transactionManager = transactionManager;
		reader = new BimReader(bimservice);
	}

	@Override
	public void openTransaction(String projectId) {
		transactionManager.open(projectId);
	}

	@Override
	public String commitTransaction() {
		return transactionManager.commit();
	}

	@Override
	public void abortTransaction() {
		transactionManager.abort();
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
			public File getFile() {
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
	public Iterable<Entity> fetchEntitiesOfType(String ifcType, String revisionId) {
		return service.getEntitiesByType(revisionId, ifcType);
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

	private void login() {
	}

	private void logout() {
	}

	@Override
	public String createCard(Entity entityToCreate, String targetProjectId) {
		final String ifcType = entityToCreate.getAttributeByName(IFC_TYPE).getValue();
		final String cmId = entityToCreate.getAttributeByName(ID_ATTRIBUTE).getValue();
		final String baseClass = entityToCreate.getAttributeByName(BASE_CLASS_NAME).getValue();
		final String globalId = entityToCreate.getAttributeByName(GLOBALID_ATTRIBUTE).getValue();
		final String code = entityToCreate.getAttributeByName(CODE_ATTRIBUTE).getValue();
		final String description = entityToCreate.getAttributeByName(DESCRIPTION_ATTRIBUTE).getValue();
		final String shapeOid = entityToCreate.getAttributeByName(SHAPE_OID).getValue();
		final String xcord = entityToCreate.getAttributeByName(X_ATTRIBUTE).getValue();
		final String ycord = entityToCreate.getAttributeByName(Y_ATTRIBUTE).getValue();
		final String zcord = entityToCreate.getAttributeByName(Z_ATTRIBUTE).getValue();

		System.out.println("Insert card " + cmId);
		System.out.println("IFC TYPE " + ifcType);
		System.out.println("BASE_CLASS_NAME " + baseClass);
		System.out.println("GLOBALID_ATTRIBUTE " + globalId);
		System.out.println("CODE_ATTRIBUTE " + code);
		System.out.println("DESCRIPTION_ATTRIBUTE " + description);
		System.out.println("SHAPE OID " + shapeOid);
		System.out.println("DEFAULT_TAG_EXPORT " + DEFAULT_TAG_EXPORT);
		System.out.println("X " + xcord);
		System.out.println("Y " + ycord);
		System.out.println("Z " + zcord);

		transactionId = transactionManager.getId();

		String objectOid = service.createObject(transactionId, ifcType);
		service.setStringAttribute(transactionId, objectOid, IFC_OBJECT_TYPE, baseClass);
		service.setStringAttribute(transactionId, objectOid, IFC_GLOBALID, globalId);
		service.setStringAttribute(transactionId, objectOid, IFC_NAME, code);
		service.setStringAttribute(transactionId, objectOid, IFC_DESCRIPTION, description);
		service.setStringAttribute(transactionId, objectOid, IFC_TAG, DEFAULT_TAG_EXPORT);
		service.setReference(transactionId, objectOid, "Representation", shapeOid);

		String placementOid = service.createObject(transactionId, IFC_LOCAL_PLACEMENT);
		service.setReference(transactionId, objectOid, IFC_OBJECT_PLACEMENT, placementOid);
		setCoordinates(placementOid, xcord, ycord, zcord, transactionId);

		return objectOid;
	}

	@Override
	public String removeCard(Entity entityToRemove, String targetProjectId) {
		transactionId = transactionManager.getId();

		final String oid = entityToRemove.getAttributeByName(OBJECT_OID).getValue();
		service.removeObject(transactionId, oid);
		return oid;
	}

	@Override
	public String findShapeWithName(String shapeName, String revisionId) {
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

	@Override
	public String getLastRevisionOfProject(String projectId) {
		final BimProject project = service.getProjectByPoid(projectId);
		return project.getLastRevisionId();
	}

	@Override
	public String fetchGlobalIdFromObjectId(final String objectId, final String revisionId) {
		Entity entity = service.getEntityByOid(revisionId, objectId);
		return entity.getKey();
	}

	@Override
	public DataHandler fetchProjectStructure(String revisionId) {
		return service.fetchProjectStructure(revisionId);

	}

	@Override
	public BimProject getProjectById(String projectId) {
		return service.getProjectByPoid(projectId);
	}

	@Override
	public Iterable<String> fetchAllGlobalIdForIfcType(String ifcType, String revisionId) {
		List<String> globalIdList = Lists.newArrayList();
		Iterable<Entity> entities = service.getEntitiesByType(revisionId, ifcType);
		for (Entity entity : entities) {
			globalIdList.add(entity.getKey());
		}
		return globalIdList;
	}

	@Override
	public Entity fetchEntityFromGlobalId(String revisionId, String globalId) {
		Entity entity = Entity.NULL_ENTITY;
		try {
			entity = service.getEntityByGuid(revisionId, globalId);
		} catch (Throwable t) {
		}
		return entity;
	}

	@Override
	public String getGlobalidFromOid(String revisionId, Long oid) {
		final String globalId = service.getGlobalidFromOid(revisionId, oid);
		return globalId;
	}

	@Override
	public String getContainerOfEntity(String globalId, String sourceRevisionId) {
		boolean exit = false;
		String containerGlobalId = StringUtils.EMPTY;
		final Iterable<Entity> allRelations = fetchEntitiesOfType(IFC_REL_CONTAINED, sourceRevisionId);
		for (final Entity relation : allRelations) {
			final ReferenceAttribute relatingStructure = ReferenceAttribute.class.cast(relation
					.getAttributeByName(IFC_RELATING_STRUCTURE));
			final ListAttribute relatedElementsAttribute = ListAttribute.class.cast(relation
					.getAttributeByName(IFC_RELATED_ELEMENTS));
			for (Attribute relatedElement : relatedElementsAttribute.getValues()) {
				if (globalId.equals(relatedElement.getValue())) {
					containerGlobalId = relatingStructure.getGlobalId();
					exit = true;
					break;
				}
			}
			if(exit){
				break;
			}
		}
		return containerGlobalId;
	}

	@Override
	public void updateRelations(Map<String, Map<String, List<String>>> relationsMap, String targetProjectId) {

		transactionId = transactionManager.getId();

		final String sourceRevisionId = service.getProjectByPoid(targetProjectId).getLastRevisionId();

		for (Entry<String, Map<String, List<String>>> entry : relationsMap.entrySet()) {
			final String spaceGuid = entry.getKey();
			final Iterable<Entity> allRelations = fetchEntitiesOfType(IFC_REL_CONTAINED, sourceRevisionId);

			Entity relation = Entity.NULL_ENTITY;
			for (final Entity rel : allRelations) {
				final ReferenceAttribute relatingStructure = ReferenceAttribute.class.cast(rel
						.getAttributeByName(IFC_RELATING_STRUCTURE));
				if (relatingStructure.getGlobalId().equals(spaceGuid)) {
					relation = rel;
					break;
				}
			}
			if (!relation.isValid()) {
				continue;
			}
			final BimserverEntity relationEntity = BimserverEntity.class.cast(relation);
			final String relationOid = relationEntity.getOid().toString();
			final ListAttribute relatedElementsAttribute = ListAttribute.class.cast(relation
					.getAttributeByName(IFC_RELATED_ELEMENTS));
			ArrayList<Long> indicesToRemove = Lists.newArrayList();
			ArrayList<Long> indicesToReadd = Lists.newArrayList();
			final int size = relatedElementsAttribute.getValues().size();

			final Map<String, List<String>> innerMap = entry.getValue();
			if (innerMap.containsKey("D")) {
				final List<String> objectsToRemove = innerMap.get("D");
				for (int i = 0; i < size; i++) {
					final Attribute relatedElement = relatedElementsAttribute.getValues().get(i);
					final String objectGuid = relatedElement.getValue();
					final Entity element = fetchEntityFromGlobalId(sourceRevisionId, objectGuid);
					if (!element.isValid()) {
						continue;
					}
					final String objectOid = BimserverEntity.class.cast(element).getOid().toString();
					if (objectsToRemove != null && objectsToRemove.contains(objectOid)) {
						indicesToRemove.add(Long.parseLong(objectOid));
					} else {
						indicesToReadd.add(Long.parseLong(objectOid));
					}
				}
				service.removeAllReferences(transactionId, relationOid, IFC_RELATED_ELEMENTS);
				System.out.println("remove all reference from relation '" + relationOid);
				for (Long indexToAdd : indicesToReadd) {
					service.addReference(transactionId, relationOid, IFC_RELATED_ELEMENTS, indexToAdd.toString());
					System.out.println("add reference '" + indexToAdd + "' to relation '" + relationOid);
				}
			}
			if (innerMap.containsKey("A")) {
				final List<String> objectsToAdd = entry.getValue().get("A");
				for (String objectToAdd : objectsToAdd) {
					service.addReference(transactionId, relationOid, IFC_RELATED_ELEMENTS, objectToAdd);
					System.out.println("add reference '" + objectToAdd + "' to relation '" + relationOid);
				}
			}
		}
	}

}
