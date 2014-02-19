package org.cmdbuild.services.bim;

import org.cmdbuild.model.bim.BimLayer;

public class BimRootCommand extends BimDataModelCommand {

	public BimRootCommand(BimDataPersistence dataPersistence, BimDataModelManager dataModelManager) {
		super(dataPersistence, dataModelManager);
	}

	@Override
	public void execute(String className, String value) {
		if (Boolean.parseBoolean(value)) {
			BimActiveCommand activeCommand = new BimActiveCommand(dataPersistence,
					dataModelManager);
			activeCommand.execute(className, value);
			BimLayer oldBimRoot = dataPersistence.findRoot();
			if (oldBimRoot != null && !oldBimRoot.getClassName().equals(className)) {
				dataModelManager.deleteBimDomainOnClass(oldBimRoot.getClassName());
				dataPersistence.saveRoot(oldBimRoot.getClassName(), false);
				dataModelManager.createBimDomainOnClass(className);
				dataPersistence.saveRoot(className, true);
			} else if (oldBimRoot == null) {
				dataModelManager.createBimDomainOnClass(className);
				dataPersistence.saveRoot(className, true);
			}
		} else {
			dataModelManager.deleteBimDomainOnClass(className); // NB: the
																// domain may
																// not exist
			dataPersistence.saveRoot(className, false);
		}
	}

}
