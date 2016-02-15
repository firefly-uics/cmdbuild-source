package org.cmdbuild.service.rest.v2.cxf.security;

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.service.rest.v2.Unauthorized;
import org.cmdbuild.service.rest.v2.cxf.service.OperationUserStore;
import org.cmdbuild.service.rest.v2.cxf.service.SessionStore;
import org.cmdbuild.service.rest.v2.cxf.util.Messages.StringFromMessage;
import org.cmdbuild.service.rest.v2.logging.LoggingSupport;
import org.cmdbuild.service.rest.v2.model.Session;

import com.google.common.base.Optional;

public class TokenHandler implements ContainerRequestFilter, LoggingSupport {

	private final StringFromMessage tokenFromMessage;
	private final SessionStore sessionStore;
	private final OperationUserStore operationUserStore;
	private final UserStore userStore;

	@Context
	private ResourceInfo resourceInfo;

	public TokenHandler(final StringFromMessage tokenFromMessage, final SessionStore sessionStore,
			final OperationUserStore operationUserStore, final UserStore userStore) {
		this.tokenFromMessage = tokenFromMessage;
		this.sessionStore = sessionStore;
		this.operationUserStore = operationUserStore;
		this.userStore = userStore;
	}

	/**
	 * Usable for tests only.
	 */
	public TokenHandler(final StringFromMessage tokenFromMessage, final SessionStore sessionStore,
			final OperationUserStore operationUserStore, final UserStore userStore, final ResourceInfo resourceInfo) {
		this(tokenFromMessage, sessionStore, operationUserStore, userStore);
		this.resourceInfo = resourceInfo;
	}

	@Override
	public void filter(final ContainerRequestContext requestContext) throws IOException {
		Response response = null;
		do {
			final boolean unauthorized = (resourceInfo.getResourceClass().getAnnotation(Unauthorized.class) != null);
			if (unauthorized) {
				break;
			}

			final Optional<String> token = tokenFromMessage.apply(requestContext);
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
		if (response != null) {
			requestContext.abortWith(response);
		}
	}

}
