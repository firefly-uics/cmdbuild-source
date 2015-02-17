package org.cmdbuild.service.rest.cxf;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.auth.UserStores.inMemory;
import static org.cmdbuild.auth.user.AuthenticatedUserImpl.ANONYMOUS_USER;
import static org.cmdbuild.service.rest.constants.Serialization.GROUP;
import static org.cmdbuild.service.rest.model.Models.newResponseSingle;
import static org.cmdbuild.service.rest.model.Models.newSession;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.context.NullPrivilegeContext;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.LoginDTO;
import org.cmdbuild.service.rest.Sessions;
import org.cmdbuild.service.rest.cxf.service.OperationUserStore;
import org.cmdbuild.service.rest.cxf.service.SessionStore;
import org.cmdbuild.service.rest.cxf.service.TokenGenerator;
import org.cmdbuild.service.rest.logging.LoggingSupport;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.cmdbuild.service.rest.model.Session;

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

	public CxfSessions(final ErrorHandler errorHandler, final TokenGenerator tokenGenerator,
			final SessionStore sessionStore, final LoginHandler loginHandler,
			final OperationUserStore operationUserStore) {
		this.errorHandler = errorHandler;
		this.tokenGenerator = tokenGenerator;
		this.sessionStore = sessionStore;
		this.loginHandler = loginHandler;
		this.operationUserStore = operationUserStore;
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
		final Optional<Session> storedSession = sessionStore.get(id);
		if (!storedSession.isPresent()) {
			errorHandler.sessionNotFound(id);
		}
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

		return newResponseSingle(Session.class) //
				.withElement(noPassword(updatedSession)) //
				.build();
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
