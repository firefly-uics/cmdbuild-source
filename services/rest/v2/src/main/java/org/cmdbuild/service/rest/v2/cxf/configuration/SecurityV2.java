package org.cmdbuild.service.rest.v2.cxf.configuration;

import static com.google.common.base.Predicates.assignableFrom;
import static com.google.common.base.Predicates.or;
import static java.util.Arrays.asList;

import org.cmdbuild.service.rest.v2.Sessions;
import org.cmdbuild.service.rest.v2.cxf.security.TokenHandler;
import org.cmdbuild.service.rest.v2.cxf.util.Messages.FirstPresentOrAbsent;
import org.cmdbuild.service.rest.v2.cxf.util.Messages.HeaderValue;
import org.cmdbuild.service.rest.v2.cxf.util.Messages.ParameterValue;
import org.cmdbuild.service.rest.v2.logging.LoggingSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Predicate;

@Configuration
public class SecurityV2 implements LoggingSupport {

	private static final String TOKEN_KEY = "CMDBuild-Authorization";

	@Autowired
	private ApplicationContextHelperV2 helper;

	@Autowired
	private ServicesV2 services;

	@Bean
	public TokenHandler v2_tokenHandler() {
		return new TokenHandler(v2_tokenFromMessage(), unauthorizedServices(), services.v2_sessionStore(),
				services.v2_operationUserStore(), helper.userStore());
	}

	@Bean
	protected FirstPresentOrAbsent v2_tokenFromMessage() {
		return FirstPresentOrAbsent.of(asList(v2_tokenFromHeader(), v2_tokenFromParameter()));
	}

	@Bean
	protected HeaderValue v2_tokenFromHeader() {
		return HeaderValue.of(TOKEN_KEY);
	}

	@Bean
	protected ParameterValue v2_tokenFromParameter() {
		return ParameterValue.of(TOKEN_KEY);
	}

	private Predicate<Class<?>> unauthorizedServices() {
		return or(assignableFrom(Sessions.class));
	}

}
