package org.cmdbuild.service.rest.v1.cxf;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.auth.UserStores.inMemory;
import static org.cmdbuild.auth.user.AuthenticatedUserImpl.ANONYMOUS_USER;
import static org.cmdbuild.service.rest.v1.constants.Serialization.GROUP;
import static org.cmdbuild.service.rest.v1.model.Models.newResponseSingle;
import static org.cmdbuild.service.rest.v1.model.Models.newSession;

import org.cmdbuild.auth.TokenGenerator;
import org.cmdbuild.auth.TokenManager;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.context.NullPrivilegeContext;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.LoginDTO;
import org.cmdbuild.service.rest.v1.Sessions;
import org.cmdbuild.service.rest.v1.cxf.service.OperationUserStore;
import org.cmdbuild.service.rest.v1.cxf.service.SessionStore;
import org.cmdbuild.service.rest.v1.logging.LoggingSupport;
import org.cmdbuild.service.rest.v1.model.ResponseSingle;
import org.cmdbuild.service.rest.v1.model.Session;

import com.google.common.base.Optional;

public class CxfSessions implements Sessions, LoggingSupport {

	public static interface LoginHandler {

		OperationUser login(LoginDTO loginDTO);

		OperationUser login(LoginDTO loginDTO, OperationUser operationUser);

	}

	public static class AuthenticationLogicAdapter implements LoginHandler {

		private static final OperationUser ANONYMOUS = new OperationUser(ANONYMOUS_USER, new NullPrivilegeContext(),
				new NullGroup());

		private final AuthenticationLogic authenticationLogic;

		public AuthenticationLogicAdapter(final AuthenticationLogic authenticationLogic) {
			this.authenticationLogic = authenticationLogic;
		}

		@Override
		public OperationUser login(final LoginDTO loginDTO) {
			return login(loginDTO, ANONYMOUS);
		}

		@Override
		public OperationUser login(final LoginDTO loginDTO, final OperationUser operationUser) {
			final UserStore temporary = inMemory(operationUser);
			authenticationLogic.login(loginDTO, temporary);
			return temporary.getUser();
		}

	}

	private final ErrorHandler errorHandler;
	private final TokenGenerator tokenGenerator;
	private final SessionStore sessionStore;
	private final LoginHandler loginHandler;
	private final OperationUserStore operationUserStore;
	private final TokenManager tokenManager;

	public CxfSessions(final ErrorHandler errorHandler, final TokenGenerator tokenGenerator,
			final SessionStore sessionStore, final LoginHandler loginHandler,
			final OperationUserStore operationUserStore, final TokenManager tokenManager) {
		this.errorHandler = errorHandler;
		this.tokenGenerator = tokenGenerator;
		this.sessionStore = sessionStore;
		this.loginHandler = loginHandler;
		this.operationUserStore = operationUserStore;
		this.tokenManager = tokenManager;
	}

	@Override
	public ResponseSingle<Session> create(final Session session) {
		if (isBlank(session.getUsername())) {
			errorHandler.missingUsername();
		}
		if (isBlank(session.getPassword())) {
			errorHandler.missingPassword();
		}

		final OperationUser user = loginHandler.login(LoginDTO.newInstance() //
				.withLoginString(session.getUsername()) //
				.withPassword(session.getPassword()) //
				.withServiceUsersAllowed(true) //
				.build());
		final CMGroup group = user.getPreferredGroup();

		final String token = tokenGenerator.generate(session.getUsername());
		final Session updatedSession = newSession(session) //
				.withId(token) //
				.withRole(group.isActive() ? group.getName() : null) //
				.withAvailableRoles(user.getAuthenticatedUser().getGroupNames()) //
				.build();
		sessionStore.put(updatedSession);
		operationUserStore.of(updatedSession).main(user);

		return newResponseSingle(Session.class) //
				.withElement(noPassword(updatedSession)) //
				.build();
	}

	@Override
	public ResponseSingle<Session> read(final String id) {
		final Optional<Session> session = sessionStore.get(id);
		if (!session.isPresent()) {
			errorHandler.sessionNotFound(id);
		}
		return newResponseSingle(Session.class) //
				.withElement(noPassword(session.get())) //
				.build();
	}

	@Override
	public ResponseSingle<Session> update(final String id, final Session session) {
		final ResponseSingle<Session> output;
		if (sessionStore.has(id)) {
			final Optional<Session> storedSession = sessionStore.get(id);
			final Optional<OperationUser> storedOperationUser = operationUserStore.of(storedSession.get()).get();
			if (!storedOperationUser.isPresent()) {
				errorHandler.userNotFound(id);
			}
			if (isBlank(session.getRole())) {
				errorHandler.missingParam(GROUP);
			}

			final Session sessionWithGroup = newSession(storedSession.get()) //
					.withRole(session.getRole()) //
					.build();
			final OperationUser user = loginHandler.login(LoginDTO.newInstance() //
					.withLoginString(sessionWithGroup.getUsername()) //
					.withPassword(sessionWithGroup.getPassword()) //
					.withGroupName(sessionWithGroup.getRole()) //
					.withServiceUsersAllowed(true) //
					.build(), storedOperationUser.get());
			final CMGroup group = user.getPreferredGroup();

			final Session updatedSession = newSession(sessionWithGroup) //
					.withRole(group.isActive() ? group.getName() : null) //
					.build();

			sessionStore.put(updatedSession);
			operationUserStore.of(updatedSession).main(user);

			output = newResponseSingle(Session.class) //
					.withElement(noPassword(updatedSession)) //
					.build();
		} else {
			final OperationUser user = tokenManager.getUser(id);
			if (user == null || user.getAuthenticatedUser().isAnonymous()) {
				errorHandler.sessionNotFound(id);
				output = null;
			} else {
				final Session _session = newSession(session) //
						.withId(id) //
						.withAvailableRoles(user.getAuthenticatedUser().getGroupNames()) //
						.withRole((user.getPreferredGroup() == null) ? null : user.getPreferredGroup().getName()) //
						.build();
				sessionStore.put(_session);
				operationUserStore.of(_session).main(user);
				output = newResponseSingle(Session.class) //
						.withElement(noPassword(_session)) //
						.build();
			}
		}
		return output;
	}

	@Override
	public void delete(final String id) {
		final Optional<Session> session = sessionStore.get(id);
		if (!session.isPresent()) {
			errorHandler.sessionNotFound(id);
		}
		final Optional<OperationUser> operationUser = operationUserStore.of(session.get()).get();
		if (!operationUser.isPresent()) {
			errorHandler.userNotFound(id);
		}
		sessionStore.remove(id);
		operationUserStore.remove(session.get());
	}

	private Session noPassword(final Session session) {
		return newSession(session) //
				.withPassword(null) //
				.build();
	}

}
