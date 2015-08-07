package org.cmdbuild.service.rest.v1.cxf;

import static com.google.common.base.Predicates.not;
import static org.cmdbuild.service.rest.v1.model.Models.newSession;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.logic.auth.LoginDTO;
import org.cmdbuild.service.rest.v1.Impersonate;
import org.cmdbuild.service.rest.v1.cxf.CxfSessions.LoginHandler;
import org.cmdbuild.service.rest.v1.cxf.service.OperationUserStore;
import org.cmdbuild.service.rest.v1.cxf.service.SessionStore;
import org.cmdbuild.service.rest.v1.model.Session;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class CxfImpersonate implements Impersonate {

	private final ErrorHandler errorHandler;
	private final LoginHandler loginHandler;
	private final SessionStore sessionStore;
	private final SessionStore impersonateSessionStore;
	private final OperationUserStore operationUserStore;
	private final Predicate<OperationUser> operationUserAllowed;

	public CxfImpersonate(final ErrorHandler errorHandler, final LoginHandler loginHandler,
			final SessionStore sessionStore, final SessionStore impersonateSessionStore,
			final OperationUserStore operationUserStore, final Predicate<OperationUser> operationUserAllowed) {
		this.errorHandler = errorHandler;
		this.loginHandler = loginHandler;
		this.sessionStore = sessionStore;
		this.impersonateSessionStore = impersonateSessionStore;
		this.operationUserStore = operationUserStore;
		this.operationUserAllowed = operationUserAllowed;
	}

	@Override
	public void start(final String id, final String username) {
		final Optional<Session> session = sessionStore.get(id);
		if (!session.isPresent()) {
			errorHandler.sessionNotFound(id);
		}
		final Session actual = session.get();
		final Optional<OperationUser> operationUser = operationUserStore.of(actual).get();
		if (!operationUser.isPresent()) {
			errorHandler.userNotFound(id);
		}
		final OperationUser current = operationUser.get();
		if (not(operationUserAllowed).apply((current))) {
			errorHandler.notAuthorized();
		}
		final OperationUser impersonated = loginHandler.login(LoginDTO.newInstance() //
				.withLoginString(username) //
				.withNoPasswordRequired() //
				.build());
		final CMGroup group = impersonated.getPreferredGroup();
		final Session updated = newSession(actual) //
				.withUsername(username) //
				.withAvailableRoles(impersonated.getAuthenticatedUser().getGroupNames()) //
				.withRole(group.isActive() ? group.getName() : null) //
				.build();
		sessionStore.put(updated);
		impersonateSessionStore.put(actual);
		operationUserStore.of(actual).impersonate(impersonated);
	}

	@Override
	public void stop(final String id) {
		final Optional<Session> session = sessionStore.get(id);
		if (!session.isPresent()) {
			errorHandler.sessionNotFound(id);
		}
		final Optional<Session> previousSession = impersonateSessionStore.get(id);
		if (!previousSession.isPresent()) {
			errorHandler.sessionNotFound(id);
		}
		final Session previous = previousSession.get();
		sessionStore.put(previous);
		impersonateSessionStore.remove(id);
		operationUserStore.of(previous).impersonate(null);
	}

}
