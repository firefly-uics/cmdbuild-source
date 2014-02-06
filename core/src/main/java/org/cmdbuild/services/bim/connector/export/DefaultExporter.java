package org.cmdbuild.services.bim.connector.export;

import java.util.List;
import java.util.Map;

import org.cmdbuild.bim.model.Catalog;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.bim.service.BimProject;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.services.bim.BimDataPersistence;
import org.cmdbuild.services.bim.BimDataView;
import org.cmdbuild.services.bim.BimServiceFacade;

import com.google.common.collect.Maps;

public class DefaultExporter implements Exporter {

	private final BimServiceFacade serviceFacade;
	private final BimDataPersistence persistence;
	private BimDataView bimDataView;

	public DefaultExporter(BimDataView dataView, BimServiceFacade bimServiceFacade, BimDataPersistence bimPersistence) {
		this.serviceFacade = bimServiceFacade;
		this.persistence = bimPersistence;
		this.bimDataView = dataView;
	}

	// TODO what about concurrent calls of this method?
	@Override
	public String export(Catalog catalog, String sourceProjectId) {

		Map<String, String> shape_name_oid_map = Maps.newHashMap();
		//TODO: this could be done after every check-in in order to make the export faster
		BimProject tmpProject = serviceFacade.prepareProjectForExport(sourceProjectId);

		List<Entity> containers = serviceFacade.fetchContainers(tmpProject.getIdentifier());
		Map<String, Long> globalid_cmdbId_map = serviceFacade.fetchAllGlobalIdForIfcType("IfcSpace",tmpProject.getIdentifier());
		String containerClassName = persistence.getContainerClassName();
		
		bimDataView.fillGlobalidIdMap(globalid_cmdbId_map, containerClassName);

		// 4. loop on the IfcSpaces of the IFC file
		for (String containerKey : globalid_cmdbId_map.keySet()) {
			
			Long containerId = globalid_cmdbId_map.get(containerKey);

			if (containerId == null) {
				System.out.println("Container card with key '" + containerKey
						+ "' not found in CMDB. Skip this container.");
				break;
			}
			System.out.println("IfcSpace has key '" + containerKey + "' and id '" + containerId + "'");
			for (EntityDefinition catalogEntry : catalog.getEntitiesDefinitions()) {
				String className = catalogEntry.getLabel();
				String containerAttributeName = catalogEntry.getContainerAttribute();
				CMQueryResult allCardsOfClassInTheRoom = bimDataView.fetchCardsOfClassInContainer(className,
						containerId, containerAttributeName);
				String shapeName = catalogEntry.getShape();
				System.out.println("Export class with shape '" + shapeName + "'");
				String shapeOid = "-1";
				if (shape_name_oid_map.containsKey(shapeName)) {
					shapeOid = shape_name_oid_map.get(shapeName);
				} else {
					shapeOid = serviceFacade.findShapeWithName(shapeName, sourceProjectId);
					shape_name_oid_map.put(shapeName, shapeOid);
				}
				if (shapeOid.equals("-1")) {
					System.out.println("shape with name '" + shapeName + "' not found");
					return "-1";
				}
				for (CMQueryRow row : allCardsOfClassInTheRoom) {
					Map<String, String> bimData = bimDataView.fetchBimDataOfRow(row, className,
							String.valueOf(containerId), containerClassName);
					serviceFacade.insertCard(bimData, sourceProjectId, catalogEntry.getTypeName(), containerKey,
							shapeOid);
				}
			}
		}
		String revisionId = serviceFacade.commitTransaction();
		System.out.println("[INFO] revision '" + revisionId + "' created");
		return revisionId;
	}

}
