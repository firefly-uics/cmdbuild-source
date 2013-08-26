package org.cmdbuild.logic.bim;

import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.services.bim.BimDataPersistence;
import org.cmdbuild.services.bim.BimServiceFacade;

public class BimMapperInfoUpdater {
	
	private final BimServiceFacade bimServiceFacade;
	private final BimDataPersistence bimDataPersistence;
	private final DataDefinitionLogic dataDefinitionLogic;
	private final CMDataView dataView;

	
	public BimMapperInfoUpdater(BimDataPersistence bimDataPersistence, BimServiceFacade bimServiceFacade,
			CMDataView dataView, DataDefinitionLogic dataDefinitionLogic) {
		this.dataDefinitionLogic = dataDefinitionLogic;
		this.dataView = dataView;
		this.bimServiceFacade = bimServiceFacade;
		this.bimDataPersistence = bimDataPersistence;
	}

	


	
	
	

}
