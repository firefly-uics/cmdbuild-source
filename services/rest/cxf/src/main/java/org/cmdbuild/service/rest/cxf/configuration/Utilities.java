package org.cmdbuild.service.rest.cxf.configuration;

import org.cmdbuild.service.rest.cxf.DefaultErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Utilities {

	@Bean
	public DefaultErrorHandler defaultErrorHandler() {
		return new DefaultErrorHandler();
	}

	@Bean
	public Helper helper() {
		return new Helper();
	}

}
