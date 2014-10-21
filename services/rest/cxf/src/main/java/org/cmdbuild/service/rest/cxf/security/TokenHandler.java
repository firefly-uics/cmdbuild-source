package org.cmdbuild.service.rest.cxf.security;

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.apache.cxf.message.Message.PROTOCOL_HEADERS;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.service.rest.cxf.service.OperationUserStore;
import org.cmdbuild.service.rest.cxf.service.SessionStore;
import org.cmdbuild.service.rest.logging.LoggingSupport;
import org.cmdbuild.service.rest.model.Session;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class TokenHandler implements RequestHandler, LoggingSupport {

	public static final String TOKEN_HEADER = "CMDBuild-Authorization";

	private static final Optional<String> ABSENT = Optional.absent();

	private final Predicate<Class<?>> unauthorizedServices;
	private final SessionStore sessionStore;
	private final OperationUserStore operationUserStore;
	private final UserStore userStore;

	public TokenHandler(final Predicate<Class<?>> unauthorizedServices, final SessionStore sessionStore,
			final OperationUserStore operationUserStore, final UserStore userStore) {
		this.unauthorizedServices = unauthorizedServices;
		this.sessionStore = sessionStore;
		this.operationUserStore = operationUserStore;
		this.userStore = userStore;
	}

	@Override
	public Response handleRequest(final Message message, final ClassResourceInfo resourceClass) {
		final Map<String, List<String>> headers = (Map<String, List<String>>) message.get(PROTOCOL_HEADERS);
		final List<String> tokens = headers.get(TOKEN_HEADER);
		final Optional<String> token = (tokens == null || tokens.isEmpty()) ? ABSENT : Optional.of(tokens.get(0));
		Response response = null;
		do {
			final boolean unauthorized = unauthorizedServices.apply(resourceClass.getServiceClass());
			if (unauthorized) {
				break;
			}

			final boolean missingToken = !token.isPresent();
			if (missingToken) {
				response = Response.status(UNAUTHORIZED).build();
				break;
			}

			final Optional<Session> session = sessionStore.get(token.get());
			final boolean missingSession = !session.isPresent();
			if (missingSession) {
				response = Response.status(UNAUTHORIZED).build();
				break;
			}

			final boolean missingOperationUser = !operationUserStore.get(session.get()).isPresent();
			if (missingOperationUser) {
				response = Response.status(UNAUTHORIZED).build();
				break;
			}

			final OperationUser operationUser = operationUserStore.get(session.get()).get();
			userStore.setUser(operationUser);
		} while (false);
		return response;
	}
}
