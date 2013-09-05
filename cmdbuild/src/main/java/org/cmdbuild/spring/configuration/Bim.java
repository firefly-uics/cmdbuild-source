package org.cmdbuild.spring.configuration;

import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.bim.service.bimserver.BimserverService;
import org.cmdbuild.bim.service.bimserver.BimserverService.Configuration;
import org.cmdbuild.config.BimProperties;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.converter.BimProjectStorableConverter;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.DataViewStore.StorableConverter;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.logic.bim.BIMLogic;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.model.bim.BimProjectInfo;
import org.cmdbuild.services.bim.BimDataModelManager;
import org.cmdbuild.services.bim.BimDataPersistence;
import org.cmdbuild.services.bim.BimServiceFacade;
import org.cmdbuild.services.bim.DefaultBimDataModelManager;
import org.cmdbuild.services.bim.DefaultBimDataPersistence;
import org.cmdbuild.services.bim.DefaultBimServiceFacade;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

@ConfigurationComponent
public class Bim {

	@Autowired
	private DataDefinitionLogic dataDefinitionLogic;

	@Autowired
	@Qualifier("system")
	private CMDataView systemDataView;

	@Bean
	protected Configuration bimConfiguration() {
		return BimProperties.getInstance();
	}

	@Bean
	public BimService bimService() {
		return new BimserverService(bimConfiguration());
	}

	@Bean
	public BIMLogic bimLogic() {
		return new BIMLogic(bimServiceFacade(), bimDataPersistence(), bimDataModelManager());
	}

	@Bean
	protected BimServiceFacade bimServiceFacade() {
		return new DefaultBimServiceFacade(bimService());
	}

	@Bean
	protected BimDataModelManager bimDataModelManager() {
		return new DefaultBimDataModelManager(systemDataView, dataDefinitionLogic);
	}

	@Bean
	protected BimDataPersistence bimDataPersistence() {
		return new DefaultBimDataPersistence(projectInfoStore(), mapperInfoStore());
	}

	@Bean
	protected Store<BimProjectInfo> projectInfoStore() {
		return new DataViewStore<BimProjectInfo>(systemDataView, BimProjectInfoConverter());
	}

	@Bean
	protected StorableConverter<BimProjectInfo> BimProjectInfoConverter() {
		return new BimProjectStorableConverter();
	}

	@Bean
	protected DataViewStore<BimLayer> mapperInfoStore() {
		return new DataViewStore<BimLayer>(systemDataView, BimMapperInfoConverter());
	}

	@Bean
	protected StorableConverter<BimLayer> BimMapperInfoConverter() {
		return new org.cmdbuild.data.converter.BimLayerConverter();
	}

}
