package org.cmdbuild.service.rest.cxf.configuration;

import static com.google.common.base.Predicates.alwaysTrue;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.service.rest.cxf.security.TokenHandler;
import org.cmdbuild.service.rest.logging.LoggingSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Predicate;

@Configuration
public class Security implements LoggingSupport {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private Services services;

	@Bean
	public TokenHandler tokenHandler() {
		return new TokenHandler(unauthorizedServices(), services.sessionStore(), services.operationUserStore(),
				userStore());
	}

	private UserStore userStore() {
		return applicationContext.getBean(UserStore.class);
	}

	private Predicate<Class<?>> unauthorizedServices() {
		// return or(assignableFrom(Sessions.class));
		return alwaysTrue();
	}

}
