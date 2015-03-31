package org.cmdbuild.service.rest.v2.cxf.configuration;

import static com.google.common.base.Predicates.assignableFrom;
import static com.google.common.base.Predicates.or;
import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.v2.cxf.security.FirstPresent.firstPresent;

import org.cmdbuild.service.rest.v2.Sessions;
import org.cmdbuild.service.rest.v2.cxf.security.FirstPresent;
import org.cmdbuild.service.rest.v2.cxf.security.HeaderTokenExtractor;
import org.cmdbuild.service.rest.v2.cxf.security.QueryStringTokenExtractor;
import org.cmdbuild.service.rest.v2.cxf.security.TokenHandler;
import org.cmdbuild.service.rest.v2.logging.LoggingSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Predicate;

@Configuration
public class SecurityV2 implements LoggingSupport {

	@Autowired
	private ApplicationContextHelperV2 helper;

	@Autowired
	private ServicesV2 services;

	@Bean
	public TokenHandler v2_tokenHandler() {
		return new TokenHandler(v2_tokenExtractor(), unauthorizedServices(), services.v2_sessionStore(),
				services.v2_operationUserStore(), helper.userStore());
	}

	@Bean
	protected FirstPresent v2_tokenExtractor() {
		return firstPresent(asList(v2_header(), v2_queryString()));
	}

	@Bean
	protected HeaderTokenExtractor v2_header() {
		return new HeaderTokenExtractor();
	}

	@Bean
	protected QueryStringTokenExtractor v2_queryString() {
		return new QueryStringTokenExtractor();
	}

	private Predicate<Class<?>> unauthorizedServices() {
		return or(assignableFrom(Sessions.class));
	}

}
