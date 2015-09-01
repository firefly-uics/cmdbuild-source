package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.ROOT;

import org.cmdbuild.services.CustomFilesStore;
import org.cmdbuild.services.WebInfFilesStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FileStore {

	@Bean
	public CustomFilesStore filesStore() {
		return new CustomFilesStore();
	}

	@Bean
	@Qualifier(ROOT)
	public WebInfFilesStore rootFilesStore() {
		return new WebInfFilesStore();
	}

}
