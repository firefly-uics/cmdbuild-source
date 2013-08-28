package org.cmdbuild.spring.configuration;

import javax.sql.DataSource;

import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.access.SystemDataAccessLogicBuilder;
import org.cmdbuild.services.DBTemplateService;
import org.cmdbuild.services.DefaultPatchManager;
import org.cmdbuild.services.FilesStore;
import org.cmdbuild.services.PatchManager;
import org.cmdbuild.services.meta.MetadataStoreFactory;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

@ConfigurationComponent
public class Other {

	@Autowired
	private DataDefinitionLogic dataDefinitionLogic;

	@Autowired
	private DataSource dataSource;

	@Autowired
	@Qualifier("root")
	private FilesStore rootFilesStore;

	@Autowired
	private SystemDataAccessLogicBuilder systemDataAccessLogicBuilder;

	@Autowired
	private DBDataView systemDataView;

	@Bean
	public PatchManager patchManager() {
		return new DefaultPatchManager( //
				dataSource, //
				systemDataView, //
				systemDataAccessLogicBuilder, //
				dataDefinitionLogic, //
				rootFilesStore);
	}

	@Bean
	public DBTemplateService templateRepository() {
		return new DBTemplateService();
	}

	@Bean
	public MetadataStoreFactory metadataStoreFactory() {
		return new MetadataStoreFactory(systemDataView);
	}

}
