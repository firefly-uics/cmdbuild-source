package org.cmdbuild.services.bim;

import org.cmdbuild.model.bim.BimLayer;

public class BimRootCommand extends BimDataModelCommand {

	public BimRootCommand(BimPersistence dataPersistence, BimDataModelManager dataModelManager) {
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
				dataPersistence.saveRootFlag(oldBimRoot.getClassName(), false);
				dataModelManager.createBimDomainOnClass(className);
				dataPersistence.saveRootFlag(className, true);
			} else if (oldBimRoot == null) {
				dataModelManager.createBimDomainOnClass(className);
				dataPersistence.saveRootFlag(className, true);
			}
		} else {
			dataModelManager.deleteBimDomainOnClass(className); // NB: the
																// domain may
																// not exist
			dataPersistence.saveRootFlag(className, false);
		}
	}

}
