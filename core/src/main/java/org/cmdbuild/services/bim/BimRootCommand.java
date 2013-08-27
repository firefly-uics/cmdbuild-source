package org.cmdbuild.services.bim;

import org.cmdbuild.model.bim.BimMapperInfo;

public class BimRootCommand extends BimDataModelCommand {

	public BimRootCommand(BimDataPersistence dataPersistence, BimDataModelManager dataModelManager) {
		super(dataPersistence,dataModelManager);
	}

	@Override
	public void execute(String className, String value) {
		if (Boolean.parseBoolean(value)) {
			BimMapperInfo oldBimRoot = dataPersistence.findBimRoot();
			if (oldBimRoot != null && !oldBimRoot.getClassName().equals(className)) {

				dataModelManager.deleteBimDomainOnClass(oldBimRoot.getClassName());
				dataPersistence.setBimRootOnClass(oldBimRoot.getClassName(), false);
				dataModelManager.createBimDomainOnClass(className);
				dataPersistence.setBimRootOnClass(className, true);

			} else if (oldBimRoot == null) {

				dataModelManager.createBimDomainOnClass(className);
				dataPersistence.setBimRootOnClass(className, true);
			}
		} else {
			dataModelManager.deleteBimDomainOnClass(className); // NB: the domain may not exist
			dataPersistence.setBimRootOnClass(className, false);
		}
	}

}
