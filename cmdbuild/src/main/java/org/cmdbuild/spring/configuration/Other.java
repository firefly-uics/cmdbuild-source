package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.ROOT;

import javax.sql.DataSource;

import org.cmdbuild.common.java.sql.DataSourceHelper;
import org.cmdbuild.common.java.sql.DefaultDataSourceHelper;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.dao.DataViewStore;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.logic.data.access.SystemDataAccessLogicBuilder;
import org.cmdbuild.services.DefaultPatchManager;
import org.cmdbuild.services.FilesStore;
import org.cmdbuild.services.PatchManager;
import org.cmdbuild.services.meta.MetadataStoreFactory;
import org.cmdbuild.services.template.store.StoreTemplateRepository;
import org.cmdbuild.services.template.store.Template;
import org.cmdbuild.services.template.store.TemplateStorableConverter;
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
	public StoreTemplateRepository templateRepository() {
		return new StoreTemplateRepository(templateStore());
	}

	@Bean
	protected Store<Template> templateStore() {
		return DataViewStore.newInstance(data.systemDataView(), templateStorableConverter());
	}

	@Bean
	protected StorableConverter<Template> templateStorableConverter() {
		return new TemplateStorableConverter();
	}

	@Bean
	public MetadataStoreFactory metadataStoreFactory() {
		return new MetadataStoreFactory(data.systemDataView());
	}

	@Bean
	public DataSourceHelper dataSourceHelper() {
		return new DefaultDataSourceHelper();
	}

}
