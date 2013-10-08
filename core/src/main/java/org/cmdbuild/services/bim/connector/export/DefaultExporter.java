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
	public void export(Catalog catalog, String projectId) {
		System.out.println("Fetch all IfcSpaces from ifc");
		List<Entity> containers = serviceFacade.fetchContainers(projectId);
		System.out.println(containers.size() + " containers found");

		BimLayer containerLayer = persistence.findContainer();
		if (containerLayer == null) {
			return;
		}
		String containerClassName = containerLayer.getClassName();
		for (Entity container : containers) {
			String containerKey = container.getKey();

			long containerId = bimDataView.getId(containerKey, containerClassName);

			if (containerId == -1) {
				System.out.println("Container card with key '" + containerKey
						+ "' not found in CMDB. Skip this container.");
				break;
			}
			System.out.println("IfcSpace has key '" + containerKey + "' and id '" + containerId + "'");
			for (EntityDefinition catalogEntry : catalog.getEntitiesDefinitions()) {
				String className = catalogEntry.getLabel();
				CMQueryResult allCardsOfClassInTheRoom = bimDataView.fetchCardsOfClassInContainer(className,
						containerId);
				for (CMQueryRow row : allCardsOfClassInTheRoom) {
					Map<String, String> bimData = bimDataView.fetchBimDataOfRow(row, className);
					serviceFacade.insertCard(bimData, projectId, catalogEntry.getTypeName(), containerKey);
				}
			}
		}
		String revisionId = serviceFacade.commitTransaction(projectId);
		System.out.println("revision " + revisionId + " created");
	}

}
