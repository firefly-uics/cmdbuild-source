package unit.cxf;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.cmdbuild.service.rest.v1.constants.Serialization.GROUP;
import static org.cmdbuild.service.rest.v1.model.Models.newSession;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.ws.rs.WebApplicationException;

import org.cmdbuild.auth.TokenGenerator;
import org.cmdbuild.auth.TokenManager;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.context.NullPrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.logic.auth.LoginDTO;
import org.cmdbuild.service.rest.v1.cxf.CxfSessions;
import org.cmdbuild.service.rest.v1.cxf.CxfSessions.LoginHandler;
import org.cmdbuild.service.rest.v1.cxf.ErrorHandler;
import org.cmdbuild.service.rest.v1.cxf.service.OperationUserStore;
import org.cmdbuild.service.rest.v1.cxf.service.OperationUserStore.BySession;
import org.cmdbuild.service.rest.v1.cxf.service.SessionStore;
import org.cmdbuild.service.rest.v1.model.ResponseSingle;
import org.cmdbuild.service.rest.v1.model.Session;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;

public class CxfSessionsTest {

	private ErrorHandler errorHandler;
	private TokenGenerator tokenGenerator;
	private SessionStore sessionStore;
	private LoginHandler loginHandler;
	private OperationUserStore operationUserStore;
	private TokenManager tokenManager;
	private BySession bySession;

	private CxfSessions cxfSessions;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		tokenGenerator = mock(TokenGenerator.class);
		sessionStore = mock(SessionStore.class);
		loginHandler = mock(LoginHandler.class);
		operationUserStore = mock(OperationUserStore.class);
		tokenManager = mock(TokenManager.class);
		bySession = mock(BySession.class);
		doReturn(bySession) //
				.when(operationUserStore).of(any(Session.class));
		cxfSessions = new CxfSessions(errorHandler, tokenGenerator, sessionStore, loginHandler, operationUserStore,
				tokenManager);
	}

	@Test(expected = WebApplicationException.class)
	public void create_missingUsernameThrowsException() throws Exception {
		// given
		final Session session = newSession() //
				.withPassword("foo") //
				.build();
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingUsername();

		// when
		cxfSessions.create(session);
	}

	@Test(expected = WebApplicationException.class)
	public void create_blankUsernameThrowsException() throws Exception {
		// given
		final Session session = newSession() //
				.withUsername(" \t") //
				.withPassword("foo") //
				.build();
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingUsername();

		// when
		cxfSessions.create(session);
	}

	@Test(expected = WebApplicationException.class)
	public void create_missingPasswordThrowsException() throws Exception {
		// given
		final Session session = newSession() //
				.withUsername("foo") //
				.build();
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingPassword();

		// when
		cxfSessions.create(session);
	}

	@Test(expected = WebApplicationException.class)
	public void create_blankPasswordThrowsException() throws Exception {
		// given
		final Session session = newSession() //
				.withUsername("foo") //
				.withPassword(" \t") //
				.build();
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingPassword();

		// when
		cxfSessions.create(session);
	}

	@Test(expected = AuthException.class)
	public void create_errorOnLoginHandlerPropagated() throws Exception {
		// given
		final Session session = newSession() //
				.withUsername("foo") //
				.withPassword("bar") //
				.build();
		doThrow(AuthExceptionType.AUTH_LOGIN_WRONG.createException()) //
				.when(loginHandler).login(any(LoginDTO.class));

		// when
		cxfSessions.create(session);
	}

	@Test
	public void create_sessionSuccessfullyCreatedStoredAndReturned() throws Exception {
		// given
		final Session session = newSession() //
				.withUsername("username") //
				.withPassword("password") //
				.build();
		final AuthenticatedUser authenticatedUser = mock(AuthenticatedUser.class);
		doReturn(newHashSet("foo", "bar", "baz")) //
				.when(authenticatedUser).getGroupNames();
		final OperationUser operationUser = new OperationUser(authenticatedUser, new NullPrivilegeContext(),
				new NullGroup());
		doReturn(operationUser) //
				.when(loginHandler).login(any(LoginDTO.class));
		doReturn("token") //
				.when(tokenGenerator).generate(anyString());

		// when
		final ResponseSingle<Session> response = cxfSessions.create(session);

		// then
		final LoginDTO expectedLogin = LoginDTO.newInstance() //
				.withLoginString(session.getUsername()) //
				.withPassword(session.getPassword()) //
				.withServiceUsersAllowed(true) //
				.build();
		final Session expectedSession = newSession(session) //
				.withId("token") //
				.withAvailableRoles(asList("foo", "bar", "baz")) //
				.build();
		verify(loginHandler).login(eq(expectedLogin));
		verify(tokenGenerator).generate(eq(session.getUsername()));
		verify(sessionStore).put(eq(expectedSession));
		verify(operationUserStore).of(eq(expectedSession));
		verify(bySession).main(any(OperationUser.class));
		verifyNoMoreInteractions(errorHandler, tokenGenerator, sessionStore, loginHandler, operationUserStore);

		assertThat(response.getElement(), equalTo(newSession(expectedSession) //
				.withPassword(null) //
				.build()));
	}

	@Test(expected = WebApplicationException.class)
	public void read_missingSessionThrowsException() throws Exception {
		// given
		doReturn(Optional.absent()) //
				.when(sessionStore).get(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).sessionNotFound(anyString());

		// when
		cxfSessions.read("token");
	}

	@Test
	public void read_passwordNotReturned() throws Exception {
		// given
		final Session session = newSession() //
				.withId("token") //
				.withUsername("username") //
				.withPassword("password") //
				.withRole("group") //
				.build();
		doReturn(Optional.of(session)) //
				.when(sessionStore).get(anyString());

		// when
		final ResponseSingle<Session> response = cxfSessions.read("token");

		// then
		verify(sessionStore).get(eq("token"));
		verifyNoMoreInteractions(errorHandler, tokenGenerator, sessionStore, loginHandler, operationUserStore);
		assertThat(response.getElement(), equalTo(newSession(session) //
				.withPassword(null) //
				.build()));
	}

	@Test(expected = WebApplicationException.class)
	public void update_missingSessionThrowsException() throws Exception {
		// given
		final Session session = newSession() //
				.withId("token") //
				.withUsername("username") //
				.withPassword("password") //
				.withRole("group") //
				.build();
		doReturn(Optional.absent()) //
				.when(sessionStore).get(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).sessionNotFound(anyString());

		// when
		cxfSessions.update("token", session);
	}

	@Test(expected = WebApplicationException.class)
	public void update_missingUserThrowsException() throws Exception {
		// given
		final Session session = newSession() //
				.withId("token") //
				// no group
				.build();
		doReturn(true) //
				.when(sessionStore).has(eq("token"));
		doReturn(Optional.of(session)) //
				.when(sessionStore).get(eq("token"));
		doReturn(Optional.absent()) //
				.when(bySession).get();
		doThrow(new WebApplicationException()) //
				.when(errorHandler).userNotFound(eq("token"));

		// when
		cxfSessions.update("token", session);
	}

	@Test(expected = WebApplicationException.class)
	public void update_invalidGroupThrowsException() throws Exception {
		// given
		final Session session = newSession() //
				.withId("token") //
				// no group
				.build();
		doReturn(true) //
				.when(sessionStore).has(eq("token"));
		doReturn(Optional.of(session)) //
				.when(sessionStore).get(eq("token"));
		final OperationUser operationUser = new OperationUser(mock(AuthenticatedUser.class),
				mock(PrivilegeContext.class), mock(CMGroup.class));
		doReturn(Optional.of(operationUser)) //
				.when(bySession).get();
		doThrow(new WebApplicationException()) //
				.when(errorHandler).missingParam(eq(GROUP));

		// when
		cxfSessions.update("token", session);
	}

	@Test
	public void update_onlyGroupUpdated() throws Exception {
		// given
		final Session oldSession = newSession() //
				.withId("old token") //
				.withUsername("old username") //
				.withPassword("old password") //
				.withRole("old group") //
				.withAvailableRoles(asList("foo", "bar", "baz")) //
				.build();
		final Session newSession = newSession() //
				.withId("new token") //
				.withRole("new group") //
				.build();
		doReturn(true) //
				.when(sessionStore).has(anyString());
		doReturn(Optional.of(oldSession)) //
				.when(sessionStore).get(anyString());
		final CMGroup group = mock(CMGroup.class);
		doReturn("guessed group").when(group).getName();
		doReturn(true).when(group).isActive();
		final OperationUser operationUser = new OperationUser(mock(AuthenticatedUser.class),
				mock(PrivilegeContext.class), group);
		doReturn(operationUser) //
				.when(loginHandler).login(any(LoginDTO.class), any(OperationUser.class));
		doReturn(Optional.of(operationUser)) //
				.when(bySession).get();

		// when
		final ResponseSingle<Session> response = cxfSessions.update("token", newSession);

		// then
		final Session expectedSession = newSession(oldSession) //
				.withRole("guessed group") //
				.build();
		verify(sessionStore).has(eq("token"));
		verify(sessionStore).get(eq("token"));
		verify(operationUserStore).of(eq(oldSession));
		verify(bySession).get();
		verify(loginHandler).login( //
				eq(LoginDTO.newInstance() //
						.withLoginString(oldSession.getUsername()) //
						.withPassword(oldSession.getPassword()) //
						.withGroupName(newSession.getRole()) //
						.withServiceUsersAllowed(true) //
						.build()), //
				eq(operationUser));
		verify(sessionStore).put(eq(expectedSession));
		verify(operationUserStore).of(eq(newSession(oldSession) //
				.withRole("guessed group") //
				.build()));
		verify(bySession).main(eq(operationUser));
		verifyNoMoreInteractions(errorHandler, tokenGenerator, sessionStore, loginHandler, operationUserStore);

		assertThat(response.getElement(), equalTo(newSession(expectedSession) //
				.withPassword(null) //
				.build()));
	}

	@Test(expected = WebApplicationException.class)
	public void delete_missingSessionThrowsException() throws Exception {
		// given
		doReturn(Optional.absent()) //
				.when(sessionStore).get(anyString());
		doThrow(new WebApplicationException()) //
				.when(errorHandler).sessionNotFound(anyString());

		// when
		cxfSessions.delete("token");
	}

	@Test
	public void delete_sessionAndUserRemovedFromStores() throws Exception {
		// given
		final Session session = newSession() //
				.withId("token") //
				.withUsername("username") //
				.withPassword("password") //
				.withRole("group") //
				.build();
		doReturn(Optional.of(session)) //
				.when(sessionStore).get(anyString());
		final OperationUser operationUser = new OperationUser(mock(AuthenticatedUser.class),
				mock(PrivilegeContext.class), mock(CMGroup.class));
		doReturn(Optional.of(operationUser)) //
				.when(bySession).get();

		// when
		cxfSessions.delete("token");

		// then
		verify(sessionStore).get(eq("token"));
		verify(operationUserStore).of(eq(session));
		verify(sessionStore).remove(eq("token"));
		verify(operationUserStore).remove(eq(session));
		verifyNoMoreInteractions(errorHandler, tokenGenerator, sessionStore, loginHandler, operationUserStore);
	}

}
