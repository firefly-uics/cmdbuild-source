package org.cmdbuild.spring.configuration;

import javax.sql.DataSource;

import org.cmdbuild.bim.service.BimService;
import org.cmdbuild.bim.service.bimserver.BimserverClient;
import org.cmdbuild.bim.service.bimserver.BimserverConfiguration;
import org.cmdbuild.bim.service.bimserver.BimserverService;
import org.cmdbuild.bim.service.bimserver.DefaultBimserverClient;
import org.cmdbuild.bim.service.bimserver.SmartBimserverClient;
import org.cmdbuild.config.BimProperties;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.converter.StorableProjectConverter;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.DataViewStore.StorableConverter;
import org.cmdbuild.data.store.NullOnNotFoundReadStore;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.logic.bim.BimLogic;
import org.cmdbuild.logic.bim.DefaultBimLogic;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.SystemDataAccessLogicBuilder;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.model.bim.StorableProject;
import org.cmdbuild.services.bim.BimDataModelManager;
import org.cmdbuild.services.bim.BimDataView;
import org.cmdbuild.services.bim.BimFacade;
import org.cmdbuild.services.bim.BimPersistence;
import org.cmdbuild.services.bim.BimStoreManager;
import org.cmdbuild.services.bim.DefaultBimDataModelManager;
import org.cmdbuild.services.bim.DefaultBimFacade;
import org.cmdbuild.services.bim.DefaultBimPersistence;
import org.cmdbuild.services.bim.DefaultBimStoreManager;
import org.cmdbuild.services.bim.DefaultRelationPersistence;
import org.cmdbuild.services.bim.DefaultTransactionManager;
import org.cmdbuild.services.bim.RelationPersistence;
import org.cmdbuild.services.bim.TransactionManager;
import org.cmdbuild.services.bim.connector.BimCardDiffer;
import org.cmdbuild.services.bim.connector.CardDiffer;
import org.cmdbuild.services.bim.connector.DefaultBimDataView;
import org.cmdbuild.services.bim.connector.DefaultBimMapper;
import org.cmdbuild.services.bim.connector.Mapper;
import org.cmdbuild.services.bim.connector.export.ExportPolicy;
import org.cmdbuild.services.bim.connector.export.MergeOnlyBeforeExport;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

@ConfigurationComponent
public class Bim {

	@Autowired
	private DataDefinitionLogic dataDefinitionLogic;

	@Autowired
	private LookupLogic lookupLogic;

	@Autowired
	private SystemDataAccessLogicBuilder dataAccessLogicBuilder;

	@Autowired
	private DataSource dataSource;

	@Autowired
	@Qualifier("system")
	private CMDataView systemDataView;

	@Bean
	protected BimserverConfiguration bimConfiguration() {
		return BimProperties.getInstance();
	}

	@Bean
	public BimserverClient bimserverClient() {
		return new SmartBimserverClient(new DefaultBimserverClient(bimConfiguration()));
	}

	@Bean
	BimService bimService() {
		return new BimserverService(bimserverClient());
	}

	@Bean
	public BimLogic bimLogic() {
		return new DefaultBimLogic(bimServiceFacade(), bimDataPersistence(), bimDataModelManager(), mapper(),
				bimDataView(), dataAccessLogic(), exportProjectStrategy());
	}

	@Bean
	public ExportPolicy exportProjectStrategy() {
		return new MergeOnlyBeforeExport(bimServiceFacade());
	}

	@Bean
	protected DataAccessLogic dataAccessLogic() {
		return dataAccessLogicBuilder.build();
	}

	@Bean
	protected BimFacade bimServiceFacade() {
		return new DefaultBimFacade(bimService(), transactionManager());
	}

	private TransactionManager transactionManager() {
		return new DefaultTransactionManager(bimService());
	}

	@Bean
	protected CardDiffer bimCardDiffer() {
		return BimCardDiffer.buildBimCardDiffer(systemDataView, lookupLogic, bimDataView());
	}

	@Bean
	protected Mapper mapper() {
		return new DefaultBimMapper(bimCardDiffer(), bimDataView());
	}

	@Bean
	protected BimDataView bimDataView() {
		return new DefaultBimDataView(systemDataView);
	}

	@Bean
	protected BimDataModelManager bimDataModelManager() {
		return new DefaultBimDataModelManager(systemDataView, dataDefinitionLogic, lookupLogic, dataSource);
	}

	@Bean
	protected BimPersistence bimDataPersistence() {
		return new DefaultBimPersistence(storeManager(), relationPersistence());
	}

	@Bean
	protected RelationPersistence relationPersistence() {
		return new DefaultRelationPersistence(systemDataView);
	}

	@Bean
	protected BimStoreManager storeManager() {
		return new DefaultBimStoreManager(projectStore(), layerStore());
	}

	@Bean
	protected Store<StorableProject> projectStore() {
		return NullOnNotFoundReadStore.of(DataViewStore.newInstance(systemDataView, storbaleProjectConverter()));
	}

	@Bean
	protected StorableConverter<StorableProject> storbaleProjectConverter() {
		return new StorableProjectConverter();
	}

	@Bean
	protected Store<BimLayer> layerStore() {
		return NullOnNotFoundReadStore.of(DataViewStore.newInstance(systemDataView, storableLayerConverter()));
	}

	@Bean
	protected StorableConverter<BimLayer> storableLayerConverter() {
		return new org.cmdbuild.data.converter.StorableLayerConverter();
	}

}
