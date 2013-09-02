package org.cmdbuild.logic.bim;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.model.bim.BimProjectInfo;
import org.cmdbuild.services.bim.BimDataModelCommand;
import org.cmdbuild.services.bim.BimDataModelCommandFactory;
import org.cmdbuild.services.bim.BimDataModelManager;
import org.cmdbuild.services.bim.BimDataPersistence;
import org.cmdbuild.services.bim.BimServiceFacade;
import org.joda.time.DateTime;

public class BIMLogic implements Logic {

	private final BimServiceFacade bimServiceFacade;
	private final BimDataPersistence bimDataPersistence;
	private final BimDataModelManager bimDataModelManager;

	public BIMLogic( //
			final BimServiceFacade bimServiceFacade, //
			final BimDataPersistence bimDataPersistence, //
			final BimDataModelManager bimDataModelManager) {

		this.bimDataPersistence = bimDataPersistence;
		this.bimServiceFacade = bimServiceFacade;
		this.bimDataModelManager = bimDataModelManager;
	}


	// CRUD operations on BimProjectInfo

	public BimProjectInfo createBimProjectInfo(BimProjectInfo projectInfo, final File ifcFile) {

		String identifier = bimServiceFacade.create(projectInfo.getName());

		projectInfo.setProjectId(identifier);
		bimDataPersistence.saveProject(projectInfo);

		if (ifcFile != null) {
			DateTime timestamp = bimServiceFacade.update(projectInfo, ifcFile);
			projectInfo.setLastCheckin(timestamp);
			bimDataPersistence.saveProject(projectInfo);
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
	 * This method can update only description, active attributes. It updated
	 * lastCheckin attribute if ifcFile != null
	 * */
	public void updateBimProjectInfo(BimProjectInfo projectInfo, final File ifcFile) {
		if (ifcFile != null) {
			DateTime timestamp = bimServiceFacade.update(projectInfo, ifcFile);
			projectInfo.setLastCheckin(timestamp);
			bimDataPersistence.saveProject(projectInfo);
		} else {
			bimServiceFacade.update(projectInfo);
			bimDataPersistence.saveProject(projectInfo);
		}
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

}
