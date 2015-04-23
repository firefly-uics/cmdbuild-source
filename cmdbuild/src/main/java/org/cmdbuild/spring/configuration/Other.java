package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.ROOT;

import javax.sql.DataSource;

import org.cmdbuild.common.java.sql.DataSourceHelper;
import org.cmdbuild.common.java.sql.DefaultDataSourceHelper;
import org.cmdbuild.logic.data.access.SystemDataAccessLogicBuilder;
import org.cmdbuild.services.DefaultPatchManager;
import org.cmdbuild.services.FilesStore;
import org.cmdbuild.services.PatchManager;
import org.cmdbuild.services.meta.DefaultMetadataStoreFactory;
import org.cmdbuild.services.meta.MetadataStoreFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Other {

	@Autowired
	private Data data;

	@Autowired
	private DataSource dataSource;

	@Autowired
	@Qualifier(ROOT)
	private FilesStore rootFilesStore;

	@Autowired
	private SystemDataAccessLogicBuilder systemDataAccessLogicBuilder;

	@Bean
	public PatchManager patchManager() {
		return new DefaultPatchManager( //
				dataSource, //
				data.systemDataView(), //
				systemDataAccessLogicBuilder, //
				data.dataDefinitionLogic(), //
				rootFilesStore);
	}

	@Bean
	public MetadataStoreFactory metadataStoreFactory() {
		return new DefaultMetadataStoreFactory(data.systemDataView());
	}

	@Bean
	public DataSourceHelper dataSourceHelper() {
		return new DefaultDataSourceHelper();
	}

}
