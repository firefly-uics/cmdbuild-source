package org.cmdbuild.services.bim.connector.export;

//package org.cmdbuild.bim.export;
//
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//
//import org.cmdbuild.bim.mapper.CardReader;
//import org.cmdbuild.bim.mapper.CMEntityKO;
//import org.cmdbuild.bim.model.Attribute;
//import org.cmdbuild.bim.model.Catalog;
//import org.cmdbuild.bim.model.Entity;
//import org.cmdbuild.bim.model.EntityDefinition;
//import org.cmdbuild.bim.service.BimError;
//import org.cmdbuild.bim.service.BimService;
//import org.cmdbuild.bim.service.bimserver.BimserverEntity;
//import org.cmdbuild.bim.service.bimserver.BimserverReferenceAttribute;
//import org.cmdbuild.bim.utils.Constants;
//import org.cmdbuild.bim.utils.KeyConversion;
//import org.cmdbuild.bim.utils.LoggerSupport;
//import org.cmdbuild.services.soap.Private;
//import org.slf4j.Logger;
//
//import com.google.common.collect.Maps;
//
//public class BimserverExporterOld implements Exporter {
//
//	private final static Logger logger = LoggerSupport.logger;
//
//	private final String revisionId; // last revision of project to update
//	private final Catalog exportConfigCatalog; // CMDB classes to export
//	private SourceWriterParams shapeParams; // revisionId of shape ifc
//											// file and XML file for
//											// copying shape
//	private final BimserverTransactionHandler transactionHandler;
//	private String transactionId = "";
//
//	private final Private cmdbProxy;
//	private final BimService bimserverService;
//	
//	private Writer writer;
//	
//	// map Container-Oid -> RelContained-Oid
//	private final Map<String, String> uniques = Maps.newHashMap();
//
//	public BimserverExporterNew(final BimService service, final Private proxy, final String targetProjectId,
//			Catalog exportConfigCatalog, final SourceWriterParams shapeParams) {
//		this.bimserverService = service;
//		this.cmdbProxy = proxy;
//		this.revisionId = service.getProjectByPoid(targetProjectId).getLastRevisionId();
//		this.exportConfigCatalog = exportConfigCatalog;
//		this.shapeParams = shapeParams;
//		this.transactionHandler = new BimserverTransactionHandler(service, targetProjectId);
//		this.writer = new BimserverWriter(service);
//	}
//
//	public BimserverExporterNew(final BimService service, final Private proxy, final String targetProjectId,
//			Catalog exportConfigCatalog) {
//		this.bimserverService = service;
//		this.cmdbProxy = proxy;
//		this.revisionId = service.getProjectByPoid(targetProjectId).getLastRevisionId();
//		this.exportConfigCatalog = exportConfigCatalog;
//		this.transactionHandler = new BimserverTransactionHandler(service, targetProjectId);
//		this.writer = new BimserverWriter(service);
//	}
//
//	@Deprecated
//	public String export(Iterable<Entity> source) {
//		Iterable<EntityDefinition> exportDefinitions = exportConfigCatalog.getEntitiesDefinitions();
//		for (Iterator<EntityDefinition> it = exportDefinitions.iterator(); it.hasNext();) {
//			EntityDefinition entityDefinition = it.next();
//			writeObjectsOfClass(source, entityDefinition.getLabel());
//		}
//		return transactionHandler.openTransaction();
//	}
//
//	@Override
//	public String export() {
//		Iterable<EntityDefinition> exportDefinitions = exportConfigCatalog.getEntitiesDefinitions();
//		for (Iterator<EntityDefinition> it = exportDefinitions.iterator(); it.hasNext();) {
//			EntityDefinition entityDefinition = it.next();
//			logger.info("Exporting objects of class " + entityDefinition.getTypeName());
//			String shapeName = entityDefinition.getShape();
//			logger.info("shape " + shapeName);
//			String shapeRevisionId = "";
//			try {
//				shapeRevisionId = bimserverService.getProjectByName(shapeName).getLastRevisionId();
//				logger.info(shapeName + " revision " + shapeRevisionId);
//			} catch (Throwable e) {
//				throw new BimError("Failed to retrieve project for shape " + shapeName);
//			}
//			shapeParams = new SourceWriterParams(shapeName + ".xml", shapeRevisionId);
//			String cmdbClassName = entityDefinition.getTypeName();
//			CardReader cardReader = new CardReader(cmdbProxy);
//			Iterable<Entity> source = cardReader.getCards(cmdbClassName);
//			writeObjectsOfClass(source, entityDefinition.getLabel());
//		}
//		return transactionHandler.commitTransaction();
//	}
//
//	private void writeObjectsOfClass(Iterable<Entity> source, String typeName) {
//		String shapeId = "";
//		for (Iterator<Entity> it = source.iterator(); it.hasNext();) {
//			Entity entity = it.next();
//			String containerGuid = entity.getContainerKey();
//			String x1 = entity.getAttributeByName("x1").getValue();
//			String x2 = entity.getAttributeByName("x2").getValue();
//			String x3 = entity.getAttributeByName("x3").getValue();
//			Entity container = Entity.NULL_ENTITY;
//			Entity cmdbContainer = Entity.NULL_ENTITY;
//			if (!containerGuid.isEmpty()) {
//				container = bimserverService.getEntityByGuid(revisionId, containerGuid);
//				cmdbContainer = new CMEntityKO(KeyConversion.getCardFromKey(cmdbProxy, containerGuid, "Stanza"));
//			}
//			if(x1.isEmpty() && x2.isEmpty() && x3.isEmpty()){
//				Attribute x = cmdbContainer.getAttributeByName("centroid_x");
//				Attribute y = cmdbContainer.getAttributeByName("centroid_y");
//				Attribute z = cmdbContainer.getAttributeByName("floor");
//				if(x.isValid() && y.isValid() && z.isValid()){
//					x1 = x.getValue();
//					x2 = y.getValue();
//					x3 = z.getValue();					
//				}
//			}
//			boolean hasAllRequiredData = container.isValid() && !(x1.isEmpty() || x2.isEmpty() || x3.isEmpty());
//			if (!hasAllRequiredData) {
//				logger.info("Card has not all required data -> skip export");
//				continue;
//			}
//
//			transactionId = transactionHandler.openTransaction();
//
//			// write object corresponding to cmdbCard
//			logger.info("Exporting object " + entity.getKey() + "...");
//			String objectId = bimserverService.createObject(transactionId, typeName);
//			bimserverService.setStringAttribute(transactionId, objectId, "ObjectType", entity.getTypeName());
//			bimserverService.setStringAttribute(transactionId, objectId, "Name", entity.getAttributeByName("Code").getValue());
//
//			bimserverService.setStringAttribute(transactionId, objectId, "GlobalId", entity.getKey());
//			bimserverService.setStringAttribute(transactionId, objectId, "Description", entity.getAttributeByName("Description")
//					.getValue());
//			bimserverService.setStringAttribute(transactionId, objectId, "Tag", Constants.EXPORTER_TAG);
//
//			// write object coordinates
//			String placementId = bimserverService.createObject(transactionId, "IfcLocalPlacement");
//			bimserverService.setReference(transactionId, objectId, "ObjectPlacement", placementId);
//
//			setCoordinates(placementId, x1, x2, x3);
//
//			setRelationWithContainer(objectId, container, placementId);
//
//			// TODO in futuro le shape saranno già copiate nel file e dovrò solo
//			// associarle agli oggetti
//			// shapeId = ((CmdbEntity)entity).getShapeId();
//
//			if (shapeId.isEmpty()) {
//				List<String> objectsId = writer.writeSourceToTarget(transactionId, shapeParams);
//				if (objectsId.size() == 0) {
//					throw new BimError("Shape not written");
//				} else if (objectsId.size() > 1) {
//					throw new BimError("More than one shape written");
//				}
//				shapeId = objectsId.get(0);
//			}
//			bimserverService.setReference(transactionId, objectId, "Representation", shapeId);
//		}
//	}
//
//	private void setRelationWithContainer(String objectId, Entity container, String placementId) {
//		if (!container.isValid()) {
//			throw new BimError("Container not found in revision " + revisionId);
//		}
//		String containerId = ((BimserverEntity) container).getOid().toString();
//
//		String relationId = "";
//		if (uniques.containsKey(containerId)) {
//			relationId = uniques.get(containerId).toString();
//		} else {
//			Iterable<Entity> relContained = bimserverService.getEntitiesByType(revisionId,
//					"IfcRelContainedInSpatialStructure");
//			for (Iterator<Entity> it1 = relContained.iterator(); it1.hasNext();) {
//				BimserverEntity rel = (BimserverEntity) it1.next();
//				if (((BimserverReferenceAttribute) rel.getAttributeByName("RelatingStructure")).getGuid().equals(
//						container.getKey())) {
//					relationId = rel.getOid().toString();
//					uniques.put(containerId, relationId);
//					break;
//				}
//			}
//		}
//		if (!relationId.isEmpty()) {
//			// logger.info("Relation found -> insert object");
//			bimserverService.addReference(transactionId, relationId, "RelatedElements", objectId);
//		} else {
//			// logger.info("Relation not found -> create relation");
//			relationId = bimserverService.createObject(transactionId, "IfcRelContainedInSpatialStructure");
//			uniques.put(containerId, relationId);
//			bimserverService.setReference(transactionId, relationId, "RelatingStructure", containerId);
//			bimserverService.addReference(transactionId, relationId, "RelatedElements", objectId);
//		}
//
//	}
//
//	private void setCoordinates(String placementId, String x1, String x2, String x3) {
//
//		double x1d = Double.parseDouble(x1);
//		double x2d = Double.parseDouble(x2);
//		double x3d = Double.parseDouble(x3);
//
//		String relativePlacementId = bimserverService.createObject(transactionId, "IfcAxis2Placement3D");
//		bimserverService.setReference(transactionId, placementId, "RelativePlacement", relativePlacementId);
//		String cartesianPointId = bimserverService.createObject(transactionId, "IfcCartesianPoint");
//		logger.info("Set coordinates " + x1d + " " + x2d + " " + x3d);
//
//		bimserverService.addDoubleAttribute(transactionId, cartesianPointId, "Coordinates", x1d);
//		bimserverService.addDoubleAttribute(transactionId, cartesianPointId, "Coordinates", x2d);
//		bimserverService.addDoubleAttribute(transactionId, cartesianPointId, "Coordinates", x3d);
//		bimserverService.setReference(transactionId, relativePlacementId, "Location", cartesianPointId);
//	}
//
// }
