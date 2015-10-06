package org.cmdbuild.spring.configuration;

import javax.sql.DataSource;

import org.cmdbuild.services.DefaultPatchManager;
import org.cmdbuild.services.FilesStore;
import org.cmdbuild.services.PatchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Migration {

	@Autowired
	private Data data;

	@Autowired
	private DataSource dataSource;

	@Autowired
	private FileStore fileStore;

	@Bean
	public PatchManager patchManager() {
		return new DefaultPatchManager(dataSource, data.systemDataView(), data.dataDefinitionLogic(), patches());
	}

	@Bean
	protected FilesStore patches() {
		return fileStore.webInfFilesStore().sub("patches");
	}

}
