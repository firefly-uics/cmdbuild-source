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
import org.cmdbuild.data.converter.BimProjectStorableConverter;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.DataViewStore.StorableConverter;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.logic.bim.BimLogic;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.SystemDataAccessLogicBuilder;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.model.bim.BimProjectInfo;
import org.cmdbuild.services.bim.BimDataModelManager;
import org.cmdbuild.services.bim.BimDataPersistence;
import org.cmdbuild.services.bim.BimDataView;
import org.cmdbuild.services.bim.BimServiceFacade;
import org.cmdbuild.services.bim.DefaultBimDataModelManager;
import org.cmdbuild.services.bim.DefaultBimDataPersistence;
import org.cmdbuild.services.bim.DefaultBimServiceFacade;
import org.cmdbuild.services.bim.connector.BimMapper;
import org.cmdbuild.services.bim.connector.DefaultBimDataView;
import org.cmdbuild.services.bim.connector.Mapper;
import org.cmdbuild.services.bim.connector.export.DefaultExport;
import org.cmdbuild.services.bim.connector.export.Export;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

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
		return new BimLogic(bimServiceFacade(), bimDataPersistence(), bimDataModelManager(), mapper(), exporter(),
				bimDataView(), dataAccessLogic());
	}

	@Bean
	protected DataAccessLogic dataAccessLogic() {
		return dataAccessLogicBuilder.build();
	}

	@Bean
	protected BimServiceFacade bimServiceFacade() {
		return new DefaultBimServiceFacade(bimService());
	}

	@Bean
	protected Mapper mapper() {
		return new BimMapper(systemDataView, lookupLogic, dataSource);
	}

	@Bean
	protected Export exporter() {
		return new DefaultExport(bimDataView(), bimServiceFacade(), bimDataPersistence());
	}

	@Bean
	protected BimDataView bimDataView() {
		return new DefaultBimDataView(systemDataView, jdbcTemplate());
	}

	protected JdbcTemplate jdbcTemplate() {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	protected BimDataModelManager bimDataModelManager() {
		return new DefaultBimDataModelManager(systemDataView, dataDefinitionLogic, lookupLogic, dataSource);
	}

	@Bean
	protected BimDataPersistence bimDataPersistence() {
		return new DefaultBimDataPersistence(projectInfoStore(), mapperInfoStore());
	}

	@Bean
	protected Store<BimProjectInfo> projectInfoStore() {
		return DataViewStore.newInstance(systemDataView, BimProjectInfoConverter());
	}

	@Bean
	protected StorableConverter<BimProjectInfo> BimProjectInfoConverter() {
		return new BimProjectStorableConverter();
	}

	@Bean
	protected DataViewStore<BimLayer> mapperInfoStore() {
		return DataViewStore.newInstance(systemDataView, BimMapperInfoConverter());
	}

	@Bean
	protected StorableConverter<BimLayer> BimMapperInfoConverter() {
		return new org.cmdbuild.data.converter.BimLayerStorableConverter();
	}

}
