package org.cmdbuild.service.rest.v1.cxf.security;

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.service.rest.v1.cxf.service.OperationUserStore;
import org.cmdbuild.service.rest.v1.cxf.service.SessionStore;
import org.cmdbuild.service.rest.v1.logging.LoggingSupport;
import org.cmdbuild.service.rest.v1.model.Session;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class TokenHandler implements RequestHandler, LoggingSupport {

	public static interface TokenExtractor {

		Optional<String> extract(Message message);

	}

	private final TokenExtractor tokenExtractor;
	private final Predicate<Class<?>> unauthorizedServices;
	private final SessionStore sessionStore;
	private final OperationUserStore operationUserStore;
	private final UserStore userStore;

	public TokenHandler(final TokenExtractor tokenExtractor, final Predicate<Class<?>> unauthorizedServices,
			final SessionStore sessionStore, final OperationUserStore operationUserStore, final UserStore userStore) {
		this.unauthorizedServices = unauthorizedServices;
		this.sessionStore = sessionStore;
		this.operationUserStore = operationUserStore;
		this.userStore = userStore;
		this.tokenExtractor = tokenExtractor;
	}

	@Override
	public Response handleRequest(final Message message, final ClassResourceInfo resourceClass) {
		Response response = null;
		do {
			final boolean unauthorized = unauthorizedServices.apply(resourceClass.getServiceClass());
			if (unauthorized) {
				break;
			}

			final Optional<String> token = tokenExtractor.extract(message);
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

			final boolean missingOperationUser = !operationUserStore.of(session.get()).get().isPresent();
			if (missingOperationUser) {
				response = Response.status(UNAUTHORIZED).build();
				break;
			}

			final OperationUser operationUser = operationUserStore.of(session.get()).get().get();
			userStore.setUser(operationUser);
		} while (false);
		return response;
	}

}
