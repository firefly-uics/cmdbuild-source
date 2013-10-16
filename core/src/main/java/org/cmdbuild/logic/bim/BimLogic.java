package org.cmdbuild.logic.bim;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.bim.mapper.xml.XmlExportCatalogFactory;
import org.cmdbuild.bim.mapper.xml.XmlImportCatalogFactory;
import org.cmdbuild.bim.model.Catalog;
import org.cmdbuild.bim.model.Entity;
import org.cmdbuild.bim.model.EntityDefinition;
import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.model.bim.BimProjectInfo;
import org.cmdbuild.services.bim.BimDataModelCommand;
import org.cmdbuild.services.bim.BimDataModelCommandFactory;
import org.cmdbuild.services.bim.BimDataModelManager;
import org.cmdbuild.services.bim.BimDataPersistence;
import org.cmdbuild.services.bim.BimDataView;
import org.cmdbuild.services.bim.BimServiceFacade;
import org.cmdbuild.services.bim.connector.Mapper;
import org.cmdbuild.services.bim.connector.export.Exporter;
import org.joda.time.DateTime;

public class BimLogic implements Logic {

	private final BimServiceFacade bimServiceFacade;
	private final BimDataPersistence bimDataPersistence;
	private final BimDataModelManager bimDataModelManager;
	private final Mapper mapper;
	private final Exporter exporter;
	private final BimDataView bimDataView;

	public BimLogic( //
			final BimServiceFacade bimServiceFacade, //
			final BimDataPersistence bimDataPersistence, //
			final BimDataModelManager bimDataModelManager, //
			final Mapper mapper, //
			final Exporter exporter, //
			final BimDataView bimDataView) {

		this.bimDataPersistence = bimDataPersistence;
		this.bimServiceFacade = bimServiceFacade;
		this.bimDataModelManager = bimDataModelManager;
		this.mapper = mapper;
		this.exporter = exporter;
		this.bimDataView = bimDataView;
	}

	// CRUD operations on BimProjectInfo

	public BimProjectInfo createBimProjectInfo(BimProjectInfo projectInfo, final File ifcFile) {

		String identifier = bimServiceFacade.createProject(projectInfo.getName());
		projectInfo.setProjectId(identifier);

		bimDataPersistence.saveProject(projectInfo);

		if (ifcFile != null) {
			uploadIfcFile(projectInfo, ifcFile);
		}

		return projectInfo;
	}

	public List<BimProjectInfo> readBimProjectInfo() {
		return bimDataPersistence.listProjectInfo();
	}

	public void disableProject(final String projectId) {
		bimServiceFacade.disableProject(projectId);
		bimDataPersistence.disableProject(projectId);
	}

	public void enableProject(final String projectId) {
		bimServiceFacade.enableProject(projectId);
		bimDataPersistence.enableProject(projectId);
	}

	/**
	 * This method can update only description, active attributes. It updates
	 * lastCheckin attribute and synchronized attribute if ifcFile != null
	 * */
	public void updateBimProjectInfo(BimProjectInfo projectInfo, final File ifcFile) {
		if (ifcFile != null) {
			uploadIfcFile(projectInfo, ifcFile);
		} else {
			bimServiceFacade.updateProject(projectInfo);
			bimDataPersistence.saveProject(projectInfo);
		}
	}

	private void uploadIfcFile(BimProjectInfo projectInfo, final File ifcFile) {
		DateTime timestamp = bimServiceFacade.updateProject(projectInfo, ifcFile);
		projectInfo.setLastCheckin(timestamp);
		projectInfo.setSynch(false);
		bimDataPersistence.saveProject(projectInfo);
	}

	// CRUD operations on BimLayer

	public List<BimLayer> readBimLayer() {
		return bimDataPersistence.listLayers();
	}

	public void updateBimLayer(String className, String attributeName, String value) {

		BimDataModelCommandFactory factory = new BimDataModelCommandFactory(bimDataPersistence, //
				bimDataModelManager);
		BimDataModelCommand dataModelCommand = factory.create(attributeName);
		dataModelCommand.execute(className, value);
	}

	// write binding between BimProjects and cards of "BimRoot" class

	public void bindProjectToCards(String projectCardId, ArrayList<String> cardsId) {
		String rootClass = bimDataPersistence.findRoot().getClassName();
		bimDataModelManager.bindProjectToCards(projectCardId, rootClass, cardsId);
	}

	// read binding between BimProjects and cards of "BimRoot" class

	public ArrayList<String> readBindingProjectToCards(String projectId, String className) {
		return bimDataModelManager.fetchCardsBindedToProject(projectId, className);
	}

	// Synchronization of data between IFC and CMDB

	public void importIfc(String projectId) {
		BimProjectInfo projectInfo = bimDataPersistence.fetchProjectInfo(projectId);

		String xmlMapping = projectInfo.getImportMapping();
		System.out.println("[DEBUG] import mapping \n " + xmlMapping);
		Catalog catalog = XmlImportCatalogFactory.withXmlStringMapper(xmlMapping).create();

		for (EntityDefinition entityDefinition : catalog.getEntitiesDefinitions()) {
			List<Entity> source = bimServiceFacade.readEntityFromProject(entityDefinition, projectInfo);
			if (source.size() > 0) {
				mapper.update(source);
			}
		}
		bimDataPersistence.setSynchronized(projectInfo, true);
	}

	// Export data from CMDB to a BimProject

	public void exportIfc(String projectId) {

		BimProjectInfo projectInfo = bimDataPersistence.fetchProjectInfo(projectId);
		String xmlMapping = projectInfo.getExportMapping();
		System.out.println("[DEBUG] export mapping \n " + xmlMapping);
		Catalog catalog = XmlExportCatalogFactory.withXmlString(xmlMapping).create();

		String revisionId = exporter.export(catalog, projectId);

		// TODO remove this, it is just for test.
		// if (!revisionId.equals("-1")) {
		// bimServiceFacade.download(projectId);
		// }

	}

	public void download(String projectId) {
		bimServiceFacade.download(projectId);
	}

}
