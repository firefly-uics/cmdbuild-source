package org.cmdbuild.service.rest.v1.cxf.configuration;

import static com.google.common.base.Predicates.assignableFrom;
import static com.google.common.base.Predicates.or;
import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.v1.cxf.security.FirstPresent.firstPresent;

import org.cmdbuild.service.rest.v1.Sessions;
import org.cmdbuild.service.rest.v1.cxf.security.FirstPresent;
import org.cmdbuild.service.rest.v1.cxf.security.HeaderTokenExtractor;
import org.cmdbuild.service.rest.v1.cxf.security.QueryStringTokenExtractor;
import org.cmdbuild.service.rest.v1.cxf.security.TokenHandler;
import org.cmdbuild.service.rest.v1.logging.LoggingSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Predicate;

@Configuration
public class SecurityV1 implements LoggingSupport {

	@Autowired
	private ApplicationContextHelperV1 helper;

	@Autowired
	private ServicesV1 services;

	@Bean
	public TokenHandler v1_tokenHandler() {
		return new TokenHandler(v1_tokenExtractor(), unauthorizedServices(), services.v1_sessionStore(),
				services.v1_operationUserStore(), helper.userStore());
	}

	@Bean
	protected FirstPresent v1_tokenExtractor() {
		return firstPresent(asList(v1_header(), v1_queryString()));
	}

	@Bean
	protected HeaderTokenExtractor v1_header() {
		return new HeaderTokenExtractor();
	}

	@Bean
	protected QueryStringTokenExtractor v1_queryString() {
		return new QueryStringTokenExtractor();
	}

	private Predicate<Class<?>> unauthorizedServices() {
		return or(assignableFrom(Sessions.class));
	}

}
