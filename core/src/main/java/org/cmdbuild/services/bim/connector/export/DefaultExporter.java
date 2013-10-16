package org.cmdbuild.services.bim.connector.export;

import java.util.List;
import java.util.Map;

import org.cmdbuild.bim.model.Catalog;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.model.bim.BimLayer;
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

	@Override
	public String export(Catalog catalog, String projectId) {

		Map<String, String> shape_name_oid_map = Maps.newHashMap();
		System.out.println("Fetch all IfcSpaces from ifc");
		List<Entity> containers = serviceFacade.fetchContainers(projectId);
		System.out.println(containers.size() + " containers found");

		BimLayer containerLayer = persistence.findContainer();
		if (containerLayer == null) {
			return "-1";
		}
		String containerClassName = containerLayer.getClassName();
		for (Entity container : containers) {
			String containerKey = container.getKey();

			long containerId = bimDataView.getId(containerKey, containerClassName);

			if (containerId == -1) {
				System.out.println("[WARN] Container card with key '" + containerKey
						+ "' not found in CMDB. Skip this container.");
				break;
			}
			System.out.println("[DEBUG] IfcSpace has key '" + containerKey + "' and id '" + containerId + "'");
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
					shapeOid = serviceFacade.findShapeWithName(shapeName, projectId);
					shape_name_oid_map.put(shapeName, shapeOid);
				}
				if (shapeOid.equals("-1")) {
					System.out.println("[WARN] shape with name '" + shapeName + "' not found");
					return "-1";
				}
				for (CMQueryRow row : allCardsOfClassInTheRoom) {
					Map<String, String> bimData = bimDataView.fetchBimDataOfRow(row, className,
							String.valueOf(containerId), containerClassName);
					serviceFacade.insertCard(bimData, projectId, catalogEntry.getTypeName(), containerKey, shapeOid);
				}
			}
		}
		String revisionId = serviceFacade.commitTransaction();
		System.out.println("[INFO] revision '" + revisionId + "' created");
		return revisionId;
	}

}
