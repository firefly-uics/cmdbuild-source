package org.cmdbuild.spring.configuration;

import org.cmdbuild.services.CustomFilesStore;
import org.cmdbuild.services.WebInfFilesStore;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

@ConfigurationComponent
public class FIleStore {

	@Bean
	public CustomFilesStore filesStore() {
		return new CustomFilesStore();
	}

	@Bean
	@Qualifier("root")
	public WebInfFilesStore rootFilesStore() {
		return new WebInfFilesStore();
	}

}
