package org.cmdbuild.service.rest.cxf.configuration;

import static com.google.common.base.Predicates.assignableFrom;
import static com.google.common.base.Predicates.or;
import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.cxf.security.FirstPresent.firstPresent;

import org.cmdbuild.service.rest.Sessions;
import org.cmdbuild.service.rest.cxf.security.FirstPresent;
import org.cmdbuild.service.rest.cxf.security.HeaderTokenExtractor;
import org.cmdbuild.service.rest.cxf.security.QueryStringTokenExtractor;
import org.cmdbuild.service.rest.cxf.security.TokenHandler;
import org.cmdbuild.service.rest.logging.LoggingSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Predicate;

@Configuration
public class Security implements LoggingSupport {

	@Autowired
	private ApplicationContextHelper helper;

	@Autowired
	private Services services;

	@Bean
	public TokenHandler tokenHandler() {
		return new TokenHandler(tokenExtractor(), unauthorizedServices(), services.sessionStore(),
				services.operationUserStore(), helper.userStore());
	}

	@Bean
	protected FirstPresent tokenExtractor() {
		return firstPresent(asList(header(), queryString()));
	}

	@Bean
	protected HeaderTokenExtractor header() {
		return new HeaderTokenExtractor();
	}

	@Bean
	protected QueryStringTokenExtractor queryString() {
		return new QueryStringTokenExtractor();
	}

	private Predicate<Class<?>> unauthorizedServices() {
		return or(assignableFrom(Sessions.class));
	}

}
