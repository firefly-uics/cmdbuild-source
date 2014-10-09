package org.cmdbuild.service.rest.cxf.configuration;

import static com.google.common.base.Predicates.assignableFrom;
import static com.google.common.base.Predicates.or;

import org.cmdbuild.service.rest.Tokens;
import org.cmdbuild.service.rest.cxf.security.TokenHandler;
import org.cmdbuild.service.rest.logging.LoggingSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Predicate;

@Configuration
public class Security implements LoggingSupport {

	@Autowired
	private Services services;

	@Bean
	public TokenHandler tokenHandler() {
		return new TokenHandler(unauthorizedServices(), services.tokenStore());
	}

	private Predicate<Class<?>> unauthorizedServices() {
		return or(assignableFrom(Tokens.class));
	}

}
