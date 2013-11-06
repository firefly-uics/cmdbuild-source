package org.cmdbuild.services.bim;

public class BimContainerCommand extends BimDataModelCommand {

	public BimContainerCommand(BimDataPersistence bimDataPersistence, BimDataModelManager dataModelManager) {
		super(bimDataPersistence, dataModelManager);
	}

	@Override
	public void execute(String className, String value) {
		// If value=true, first of all perform ActiveCommand.
		if (Boolean.parseBoolean(value)) {
			BimActiveCommand activeCommand = new BimActiveCommand(dataPersistence, dataModelManager);
			activeCommand.execute(className, value);
			dataModelManager.addGeometryRoomFieldsIfNeeded(className);
		}
		dataPersistence.saveContainerStatus(className, value);
	}

}
