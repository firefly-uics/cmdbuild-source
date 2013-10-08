package org.cmdbuild.services.bim;

import static org.cmdbuild.bim.utils.BimConstants.DEFAULT_TAG_EXPORT;
import static org.cmdbuild.bim.utils.BimConstants.GLOBALID;
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
import static org.cmdbuild.common.Constants.BASE_CLASS_NAME;
import static org.cmdbuild.services.bim.connector.DefaultBimDataView.CODE;
import static org.cmdbuild.services.bim.connector.DefaultBimDataView.DESCRIPTION;
import static org.cmdbuild.services.bim.connector.DefaultBimDataView.ID;
import static org.cmdbuild.services.bim.connector.DefaultBimDataView.X_COORD;
import static org.cmdbuild.services.bim.connector.DefaultBimDataView.Y_COORD;
import static org.cmdbuild.services.bim.connector.DefaultBimDataView.Z_COORD;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cmdbuild.bim.mapper.Reader;
import org.cmdbuild.bim.mapper.xml.BimReader;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.bim.service.BimRevision;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.bim.service.bimserver.BimserverEntity;
import org.cmdbuild.bim.service.bimserver.BimserverReferenceAttribute;
import org.cmdbuild.model.bim.BimProjectInfo;
import org.joda.time.DateTime;

import com.google.common.collect.Maps;

public class DefaultBimServiceFacade implements BimServiceFacade {

	private final Map<String, String> container_relation_Map = Maps.newHashMap();

	private static final String IFC_SPACE = "IfcSpace";
	private final BimService service;
	private final Reader reader;

	public DefaultBimServiceFacade(BimService bimservice) {
		this.service = bimservice;
		reader = new BimReader(bimservice);
	}

	@Override
	public String createProject(final String projectName) {

		login();
		String projectId = service.createProject(projectName).getIdentifier();
		logout();

		return projectId;
	}

	@Override
	public void disableProject(final String projectId) {

		login();
		service.disableProject(projectId);
		logout();

	}

	@Override
	public void download(String projectId) {
		String revisionId = service.getProjectByPoid(projectId).getLastRevisionId();
		service.downloadIfc(revisionId);
	}

	@Override
	public void enableProject(final String projectId) {

		login();
		service.enableProject(projectId);
		logout();

	}

	@Override
	public List<Entity> fetchContainers(String projectId) {
		login();
		String revisionId = service.getProjectByPoid(projectId).getLastRevisionId();
		List<Entity> entities = service.getEntitiesByType(revisionId, IFC_SPACE);
		logout();
		return entities;
	}

	@Override
	public String fetchShapeRevision(String shapeName) {
		throw new RuntimeException("fetchShapeRevision not implemented");
	}

	@Override
	public List<Entity> readEntityFromProject(EntityDefinition entityDefinition, BimProjectInfo projectInfo) {
		login();
		String revisionId = service.getProjectByPoid(projectInfo.getProjectId()).getLastRevisionId();
		List<Entity> source = reader.readEntities(revisionId, entityDefinition);
		logout();
		return source;
	}

	@Deprecated
	public BimService service() {
		return service;
	}

	@Override
	public void updateProject(final BimProjectInfo updatedProjectInfo) {

		login();
		updateStatus(updatedProjectInfo);
		logout();

	}

	@Override
	public DateTime updateProject(final BimProjectInfo projectInfo, final File ifcFile) {

		login();

		updateStatus(projectInfo);

		String projectId = projectInfo.getIdentifier();
		service.checkin(projectInfo.getIdentifier(), ifcFile);
		final BimProject updatedProject = service.getProjectByPoid(projectId);
		final BimRevision lastRevision = service.getRevision(updatedProject.getLastRevisionId());
		if (lastRevision == null) {
			throw new BimError("Upload failed");
		}
		DateTime checkinTimeStamp = new DateTime(lastRevision.getDate().getTime());
		logout();

		return checkinTimeStamp;
	}

	@Override
	public void writeCardIntoProject() {
		throw new RuntimeException("writeCardIntoProject not implemented");
	}

	private void login() {
	}

	private void logout() {
	}

	private void updateStatus(final BimProjectInfo projectInfo) {
		String projectId = projectInfo.getIdentifier();
		BimProject bimProject = service.getProjectByPoid(projectId);

		if (bimProject.isActive() != projectInfo.isActive()) {
			if (projectInfo.isActive()) {
				service.enableProject(projectId);
			} else {
				service.disableProject(projectId);
			}
		}
	}

	@Override
	public void insertCard(Map<String, String> bimData, String projectId, String ifcType, String containerKey) {
		// FIXME manage transactions
		String revisionId = service.getProjectByPoid(projectId).getLastRevisionId();

		String transactionId = openTransaction(projectId);
		System.out.println("Writing card " + bimData.get(ID));
		String objectOid = service.createObject(transactionId, ifcType);
		service.setStringAttribute(transactionId, objectOid, IFC_OBJECT_TYPE, bimData.get(BASE_CLASS_NAME));
		service.setStringAttribute(transactionId, objectOid, IFC_GLOBALID, bimData.get(GLOBALID));
		service.setStringAttribute(transactionId, objectOid, IFC_NAME, bimData.get(CODE));
		service.setStringAttribute(transactionId, objectOid, IFC_DESCRIPTION, bimData.get(DESCRIPTION));
		service.setStringAttribute(transactionId, objectOid, IFC_TAG, DEFAULT_TAG_EXPORT);

		// write coordinates
		String placementOid = service.createObject(transactionId, IFC_LOCAL_PLACEMENT);
		service.setReference(transactionId, objectOid, IFC_OBJECT_PLACEMENT, placementOid);
		setCoordinates(placementOid, bimData.get(X_COORD), bimData.get(Y_COORD), bimData.get(Z_COORD), transactionId);
		setRelationWithContainer(objectOid, containerKey, placementOid, revisionId, transactionId);

		// String shapeId = null;
		// service.setReference(transactionId, objectId,
		// "Representation", shapeId);
	}

	private String openTransaction(String projectId) {

		return null;
	}

	@Override
	public String commitTransaction(String projectId) {

		return null;
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
			// logger.info("Relation found -> insert object");
			service.addReference(transactionId, relationOid, IFC_RELATED_ELEMENTS, objectId);
		} else {
			// logger.info("Relation not found -> create relation");
			relationOid = service.createObject(transactionId, IFC_REL_CONTAINED);
			container_relation_Map.put(containerOid, relationOid);
			service.setReference(transactionId, relationOid, IFC_RELATING_STRUCTURE, containerOid);
			service.addReference(transactionId, relationOid, IFC_RELATED_ELEMENTS, objectId);
		}

	}

	private class TransactionHandler {

		private String transactionId = "";

		public String openTransaction(String projectId) {
			if (transactionId.isEmpty()) {
				transactionId = service.openTransaction(projectId);
			}
			return transactionId;
		}

		protected String commitTransaction() {
			if (!transactionId.isEmpty()) {
				try {
					String newRevisionId = service.commitTransaction(transactionId);
					return newRevisionId;
				} catch (Exception e) {
					service.abortTransaction(transactionId);
					return "-1";
				}
			} else {
				return "-1";
			}
		}

	}

}
