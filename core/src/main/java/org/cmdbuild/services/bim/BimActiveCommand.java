package org.cmdbuild.services.bim;


public class BimActiveCommand extends BimDataModelCommand {

	public BimActiveCommand(BimDataPersistence bimDataPersistence, BimDataModelManager dataModelManager) {
		super(bimDataPersistence, dataModelManager);
	}

	@Override
	public void execute(String className, String value) {

		if (Boolean.parseBoolean(value)) {
			dataModelManager.createBimTableIfNeeded(className);
		}
		dataPersistence.setActive(className, value);
	}

}
