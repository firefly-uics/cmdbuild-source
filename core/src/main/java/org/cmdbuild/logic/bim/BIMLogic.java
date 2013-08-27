package org.cmdbuild.logic.bim;

import java.io.File;
import java.util.List;

import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.bim.BimMapperInfo;
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
		bimDataPersistence.store(projectInfo);

		DateTime timestamp = bimServiceFacade.update(projectInfo, ifcFile);
		projectInfo.setLastCheckin(timestamp);
		bimDataPersistence.store(projectInfo);

		return projectInfo;
	}

	public List<BimProjectInfo> readBimProjectInfo() {
		return bimDataPersistence.readBimProjectInfo();
	}

	public void disableProject(final String projectId) {
		bimServiceFacade.disableProject(projectId);
		bimDataPersistence.disableProject(projectId);
	}

	public void enableProject(final String projectId) {
		bimServiceFacade.enableProject(projectId);
		bimDataPersistence.enableProject(projectId);
	}

	public void updateBimProjectInfo(BimProjectInfo projectInfo, final File ifcFile) {
		if (ifcFile != null) {
			DateTime timestamp = bimServiceFacade.update(projectInfo, ifcFile);
			projectInfo.setLastCheckin(timestamp);
			bimDataPersistence.store(projectInfo);
		} else {
			bimServiceFacade.update(projectInfo);
			bimDataPersistence.store(projectInfo);
		}
	}

	// CRUD operations on BimMapperInfo

	public List<BimMapperInfo> readBimMapperInfo() {
		return bimDataPersistence.readBimMapperInfo();
	}

	public void updateBimMapperInfo(String className, String attributeName, String value) {

		BimDataModelCommandFactory factory = new BimDataModelCommandFactory(bimDataPersistence, //
				bimDataModelManager);
		BimDataModelCommand dataModelCommand = factory.create(attributeName);
		dataModelCommand.execute(className, value);

	}

}
