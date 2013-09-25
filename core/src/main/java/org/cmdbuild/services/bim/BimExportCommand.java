package org.cmdbuild.services.bim;

public class BimExportCommand extends BimDataModelCommand {

	public BimExportCommand(BimDataPersistence dataPersistence,
			BimDataModelManager dataModelManager) {
		super(dataPersistence, dataModelManager);
	}

	@Override
	public void execute(String className, String value) {
		// If value=true, first of all perform ActiveCommand.
		if (Boolean.parseBoolean(value)) {
			BimActiveCommand activeCommand = new BimActiveCommand(dataPersistence,
					dataModelManager);
			activeCommand.execute(className, value);
			dataModelManager.addGeometryFieldIfNeeded(className);
		}
		dataPersistence.saveExportStatus(className, value);
	}

}
