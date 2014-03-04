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
import static org.cmdbuild.bim.utils.BimConstants.OBJECT_OID;
import static org.cmdbuild.bim.utils.BimConstants.X_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.Y_ATTRIBUTE;
import static org.cmdbuild.bim.utils.BimConstants.Z_ATTRIBUTE;
import static org.cmdbuild.common.Constants.BASE_CLASS_NAME;
import static org.cmdbuild.common.Constants.CODE_ATTRIBUTE;
import static org.cmdbuild.common.Constants.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;
import static org.cmdbuild.services.bim.connector.DefaultBimDataView.IFC_TYPE;
import static org.cmdbuild.services.bim.connector.DefaultBimDataView.SHAPE_OID;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
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
import org.cmdbuild.bim.service.bimserver.BimserverListAttribute;
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
	public Iterable<Entity> fetchEntitiesOfType(String ifcType, String revisionId) {
		return service.getEntitiesByType(revisionId, ifcType);
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
	public String createCard(Entity entityToCreate, String targetProjectId) {

		if (transactionId.equals(NULL_TRANSACTION_ID)) {
			transactionId = service.openTransaction(targetProjectId);
			System.out.println("*************** Transaction " + transactionId + " opened at " + new DateTime());
		}

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

		// setRelationWithContainer(objectOid, spaceGlobalId,
		// targetProject.getLastRevisionId(), transactionId);

		return objectOid;
	}

	@Override
	public void createCard(Entity cardData, String targetProjectId, String ifcType, String containerKey, String shapeOid) {

		final BimProject targetProject = service.getProjectByPoid(targetProjectId);

		if (transactionId.equals(NULL_TRANSACTION_ID)) {
			transactionId = service.openTransaction(targetProjectId);
			System.out.println("*************** Transaction " + transactionId + " opened at " + new DateTime());
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

		String placementOid = service.createObject(transactionId, IFC_LOCAL_PLACEMENT);
		service.setReference(transactionId, objectOid, IFC_OBJECT_PLACEMENT, placementOid);
		setCoordinates(placementOid, cardData.getAttributeByName(X_ATTRIBUTE).getValue(),
				cardData.getAttributeByName(Y_ATTRIBUTE).getValue(), cardData.getAttributeByName(Z_ATTRIBUTE)
						.getValue(), transactionId);

		setRelationWithContainer(objectOid, containerKey, targetProject.getLastRevisionId(), transactionId);
		service.setReference(transactionId, objectOid, "Representation", shapeOid);
	}

	@Override
	public String removeCard(Entity entityToRemove, String targetProjectId) {
		if (transactionId.equals(NULL_TRANSACTION_ID)) {
			transactionId = service.openTransaction(targetProjectId);
			System.out.println("*************** Transaction " + transactionId + " opened at " + new DateTime());
		}
		final String oid = entityToRemove.getAttributeByName(OBJECT_OID).getValue();
		service.removeObject(transactionId, oid);
		return oid;
	}

	@Override
	public void removeCard(Entity entity, String projectId, String containerKey) {

		if (transactionId.equals(NULL_TRANSACTION_ID)) {
			transactionId = service.openTransaction(projectId);
			System.out.println("*************** Transaction " + transactionId + " opened at " + new DateTime());
		}
		System.out.println("Delete card " + entity + "...");
		final String revisionId = service.getProjectByPoid(projectId).getLastRevisionId();

		removeRelationWithContainer(entity, containerKey, revisionId);

		service.removeObject(transactionId, revisionId, entity.getKey());

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

	@Override
	public String commitTransaction() {
		System.out.println("*************** Commit transaction " + transactionId + "...");
		String revisionId = NULL_REVISION_ID;
		if (transactionId.equals(NULL_TRANSACTION_ID)) {
			return NULL_REVISION_ID;
		}
		try {
			revisionId = service.commitTransaction(transactionId);
			System.out.println("*************** Transaction " + transactionId + " committed at " + new DateTime());
		} finally {
			// FIXME transactionId is not reset if the commit fails!!!!
			transactionId = NULL_TRANSACTION_ID;
			System.out.println("*************** Transaction resetted to " + transactionId);
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

	private void removeRelationWithContainer(final Entity entity, final String containerKey, final String revisionId) {
		final String relationOid = getRelationOidFromContainerOid(containerKey, revisionId);
		BimserverEntity relationEntity = BimserverEntity.class.cast(service.getEntityByOid(revisionId, relationOid));
		final BimserverListAttribute relatedElements = BimserverListAttribute.class.cast(relationEntity
				.getAttributeByName(IFC_RELATED_ELEMENTS));
		int indexToRemove = getIndexOfObjectInRelation(entity, relationOid, revisionId);
		System.out.println("index to remove is " + indexToRemove);
		if (indexToRemove != -1) {
			int numberOfElements = relatedElements.getValues().size();
			System.out.println("there are " + numberOfElements + " elements");
			List<Long> objectsToAdd = Lists.newArrayList();
			for (int i = numberOfElements - 1; i >= indexToRemove; i--) {
				System.out.println("i = " + i);
				long elementOid = ReferenceAttribute.class.cast(relatedElements.getValues().get(i)).getOid();
				System.out.println("oid is " + elementOid);
				service.removeReference(transactionId, relationOid, IFC_RELATED_ELEMENTS, i);
				System.out.println(i + "th element removed from relation");
				if (i != indexToRemove) {
					objectsToAdd.add(elementOid);
					System.out.println("element " + i + " has to be added when finished");
				} else {
					System.out.println("i is " + i + " should be the last");
				}
			}
			System.out.println("finished to remove elements");
			for (Long object : objectsToAdd) {
				service.addReference(transactionId, relationOid, IFC_RELATED_ELEMENTS, object.toString());
				System.out.println("element " + object + " was added");
			}
		} else {
			// some log...
		}
	}

	private int getIndexOfObjectInRelation(Entity entity, String relationOid, String revisionId) {
		final String objectKey = entity.getKey();
		final BimserverEntity relation = (BimserverEntity.class.cast(service.getEntityByOid(revisionId, relationOid)));
		final BimserverListAttribute relatedElements = BimserverListAttribute.class.cast(relation
				.getAttributeByName(IFC_RELATED_ELEMENTS));
		int index = 0;
		for (Attribute element : relatedElements.getValues()) {
			ReferenceAttribute elementReference = ReferenceAttribute.class.cast(element);
			if (elementReference.getGlobalId().equals(objectKey)) {
				return index;
			}
			index = index + 1;
		}
		return -1;
	}

	private Entity getIfcSpaceFromGlobalIdHandlingStrangeExceptionsFromBimserver(final String containerKey,
			final String revisionId) {
		Entity container = Entity.NULL_ENTITY;
		try {
			container = service.getEntityByGuid(revisionId, containerKey);
		} catch (Throwable t) {
			Iterable<Entity> containerList = service.getEntitiesByType(revisionId, "IfcSpace");
			for (Entity cont : containerList) {
				if (cont.getKey().equals(containerKey)) {
					container = cont;
					break;
				}
			}
		}
		return container;
	}

	private String getRelationOidFromContainerOid(final String containerKey, final String revisionId) {
		Entity container = getIfcSpaceFromGlobalIdHandlingStrangeExceptionsFromBimserver(containerKey, revisionId);
		final String containerOid = (BimserverEntity.class.cast(container)).getOid().toString();
		String relationOid = StringUtils.EMPTY;
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
		return relationOid;
	}

	private void setRelationWithContainer(String objectId, String containerKey, String revisionId, String transactionId) {

		Entity container = getIfcSpaceFromGlobalIdHandlingStrangeExceptionsFromBimserver(containerKey, revisionId);
		String containerOid = oidOf(container);
		String relationOid = getRelationOidFromContainerOid(containerKey, revisionId);
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
		return entity.getKey();
	}

	@Override
	public Map<String, Long> getGlobalidOidMap(String revisionId) {
		Map<String, Long> globalIdMap = service.getGlobalIdOidMap(revisionId);
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
	public Iterable<String> fetchAllGlobalIdForIfcType(String ifcType, String projectId) {
		List<String> globalIdList = Lists.newArrayList();
		String revisionId = service.getProjectByPoid(projectId).getLastRevisionId();
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

	private static String oidOf(Entity entity) {
		String oid = StringUtils.EMPTY;
		if (entity.isValid() && entity instanceof BimserverEntity) {
			oid = ((BimserverEntity) entity).getOid().toString();
		}
		return oid;
	}

	@Override
	public String getContainerOfEntity(String globalId, String sourceRevisionId) {
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
					break;
				}
			}
		}
		return containerGlobalId;
	}

	@Override
	public void updateRelations(Map<String, Map<String, List<String>>> relationsMap, String targetProjectId) {
		final String sourceRevisionId = service.getProjectByPoid(targetProjectId).getLastRevisionId();

		for (Entry<String, Map<String, List<String>>> entry : relationsMap.entrySet()) {
			final String spaceGuid = entry.getKey();
			final Map<String, List<String>> innerMap = entry.getValue();
			final List<String> objectsToRemove = entry.getValue().get("D");
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
			for (int i = 0; i < size; i++) {
				final Attribute relatedElement = relatedElementsAttribute.getValues().get(i);
				final String objectGuid = relatedElement.getValue();
				final Entity element = fetchEntityFromGlobalId(sourceRevisionId, objectGuid);
				if (!element.isValid()) {
					continue;
				}
				final String objectOid = ((BimserverEntity) element).getOid().toString();
				if (objectsToRemove != null && objectsToRemove.contains(objectOid)) {
					indicesToRemove.add(Long.parseLong(objectOid));
				} else {
					indicesToReadd.add(Long.parseLong(objectOid));
				}
			}
			service.removeAllReferences(transactionId, relationOid, IFC_RELATED_ELEMENTS);
			for (Long indexToAdd : indicesToReadd) {
				service.addReference(transactionId, relationOid, IFC_RELATED_ELEMENTS, indexToAdd.toString());
			}
			if (innerMap.containsKey("A")) {
				final List<String> objectsToAdd = entry.getValue().get("A");
				for (String objectToAdd : objectsToAdd) {
					service.addReference(transactionId, relationOid, IFC_RELATED_ELEMENTS, objectToAdd);
				}
			}
		}
	}
}
