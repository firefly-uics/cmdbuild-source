package org.cmdbuild.service.rest.cxf.security;

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.apache.cxf.message.Message.PROTOCOL_HEADERS;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.cmdbuild.service.rest.logging.LoggingSupport;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class TokenHandler implements RequestHandler, LoggingSupport {

	public static final String TOKEN_HEADER = "CMDBuild-Authorization";

	private static final Optional<String> ABSENT = Optional.absent();

	private final Predicate<Class<?>> unauthorizedServices;

	public TokenHandler(final Predicate<Class<?>> unauthorizedServices) {
		this.unauthorizedServices = unauthorizedServices;
	}

	@Override
	public Response handleRequest(final Message message, final ClassResourceInfo resourceClass) {
		final Map<String, List<String>> headers = (Map<String, List<String>>) message.get(PROTOCOL_HEADERS);
		final List<String> tokens = headers.get(TOKEN_HEADER);
		final Optional<String> token = (tokens == null || tokens.isEmpty()) ? ABSENT : Optional.of(tokens.get(0));
		final boolean unauthorized = unauthorizedServices.apply(resourceClass.getServiceClass());
		final Response response;
		if (!unauthorized && !token.isPresent()) {
			response = Response.status(UNAUTHORIZED).build();
		} else {
			response = null;
		}
		return response;
	}

}
