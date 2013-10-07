package org.cmdbuild.services.bim.connector.export;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.cmdbuild.bim.model.Catalog;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.services.bim.BimDataPersistence;
import org.cmdbuild.services.bim.BimDataView;
import org.cmdbuild.services.bim.BimServiceFacade;
import org.cmdbuild.services.bim.DefaultBimServiceFacade;
import org.cmdbuild.services.bim.connector.BimMapperRules;
import org.cmdbuild.services.bim.connector.DefaultBimDataView;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.cmdbuild.services.bim.connector.DefaultBimDataView.*;
import static org.cmdbuild.bim.utils.BimConstants.*;

public class BimExporter implements Exporter {


	private EntityDefinition entityDefinition;
	private final CMDataView dataView;
	private final BimServiceFacade serviceFacade;
	private final BimDataPersistence persistence;
	private final BimService service;
	private BimDataView bimDataView;
	private final JdbcTemplate jdbcTemplate;

	public BimExporter(CMDataView systemDataView, BimServiceFacade bimServiceFacade, DataSource dataSource, BimDataPersistence bimPersistence) {
		this.dataView = systemDataView;
		this.serviceFacade = bimServiceFacade;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.persistence = bimPersistence;
		this.bimDataView = new DefaultBimDataView(dataView, null);
		
		
		//TODO remove this and use the facade
		service = ((DefaultBimServiceFacade) serviceFacade).service();
	}

	@Override
	@Deprecated
	public String export(EntityDefinition entityDefinition, String projectId) {
		return null;
	}

	private CMQueryResult getCardsToExport() {
		String className = entityDefinition.getTypeName();
		CMClass theClass = dataView.findClass(className);
		CMQueryResult queryResult = dataView.select(anyAttribute(theClass)) //
				.from(theClass) //
				.run();
		return queryResult;
	}

	@Override
	public void export(Catalog catalog, String projectId) {
		System.out.println("Fetch all IfcSpaces from ifc");
		List<Entity> containers = serviceFacade.fetchContainers(projectId);
		System.out.println(containers.size() + " containers found");
		
		BimLayer containerLayer = persistence.findContainer();
		if(containerLayer == null){
			return;
		}
		String containerClassName = containerLayer.getClassName();
		for (Entity container : containers) {
			String key = container.getKey();
			
			long containerId = BimMapperRules.INSTANCE.convertKeyToId(key, containerClassName, dataView);
			if(containerId == -1){
				System.out.println("Container card with key " + key + " not found in CMDB. Skip this container.");
				break;
			}
			System.out.println("IfcSpace has key '" + key + "' and id '" + containerId + "'");
			for (EntityDefinition catalogEntry : catalog.getEntitiesDefinitions()) {
				String className = catalogEntry.getLabel();
				
				CMQueryResult allCardsOfClassInTheRoom = bimDataView.fetchCardsOfClassInContainer(className, containerId);   
				for (CMQueryRow row : allCardsOfClassInTheRoom) {
					
					Map<String, String> bimData = bimDataView.fetchBimDataOfRow(row, className);
					
					//insert card into IFC
					BimService service = ((DefaultBimServiceFacade) serviceFacade).service();
					
					serviceFacade.insertCard(bimData, projectId);
					
					
					//FIXME manage transactions
					String transactionId = service.openTransaction(projectId);
					
					
					
					System.out.println("Writing card " + bimData.get(ID)); 
					String objectId = service.createObject(transactionId, catalogEntry.getTypeName());
					service.setStringAttribute(transactionId, objectId, "ObjectType", catalogEntry.getTypeName());
					service.setStringAttribute(transactionId, objectId, "GlobalId", bimData.get(GLOBALID));
					service.setStringAttribute(transactionId, objectId, "Name", bimData.get(CODE)); 
					service.setStringAttribute(transactionId, objectId, "Description", bimData.get(DESCRIPTION));
					service.setStringAttribute(transactionId, objectId, "Tag", DEFAULT_TAG_EXPORT);

					// write coordinates
					String placementId = service.createObject(transactionId, "IfcLocalPlacement");
					service.setReference(transactionId, objectId, "ObjectPlacement", placementId);
					setCoordinates(placementId, coords[0], coords[1], coords[2], transactionId);
					setRelationWithContainer(objectId, container, placementId);
					// String shapeId = null;
					// service.setReference(transactionId, objectId,
					// "Representation", shapeId);

					String revisionId = service.commitTransaction(transactionId);
					System.out.println("revision " + revisionId + " created");
				}
			}
		}
	}

	private void setRelationWithContainer(String objectId, Entity container, String placementId) {
		// TODO Auto-generated method stub

	}

	private void setCoordinates(String placementId, String x1, String x2, String x3, String transactionId) {
		double x1d = Double.parseDouble(x1);
		double x2d = Double.parseDouble(x2);
		double x3d = Double.parseDouble(x3);

		String relativePlacementId = service.createObject(transactionId, "IfcAxis2Placement3D");
		service.setReference(transactionId, placementId, "RelativePlacement", relativePlacementId);
		String cartesianPointId = service.createObject(transactionId, "IfcCartesianPoint");
		System.out.println("Set coordinates " + x1d + " " + x2d + " " + x3d);
		service.addDoubleAttribute(transactionId, cartesianPointId, "Coordinates", x1d);
		service.addDoubleAttribute(transactionId, cartesianPointId, "Coordinates", x2d);
		service.addDoubleAttribute(transactionId, cartesianPointId, "Coordinates", x3d);
		service.setReference(transactionId, relativePlacementId, "Location", cartesianPointId);
	}
}
