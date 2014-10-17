package unit.cxf;

import static org.cmdbuild.service.rest.constants.Serialization.GROUP;
import static org.cmdbuild.service.rest.model.Builders.newSession;
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
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.LoginDTO;
import org.cmdbuild.service.rest.cxf.CxfSessions;
import org.cmdbuild.service.rest.cxf.ErrorHandler;
import org.cmdbuild.service.rest.cxf.service.OperationUserStore;
import org.cmdbuild.service.rest.cxf.service.SessionStore;
import org.cmdbuild.service.rest.cxf.service.TokenGenerator;
import org.cmdbuild.service.rest.model.ResponseSingle;
import org.cmdbuild.service.rest.model.Session;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.common.base.Optional;

public class CxfSessionsTest {

	private ErrorHandler errorHandler;
	private TokenGenerator tokenGenerator;
	private SessionStore sessionStore;
	private AuthenticationLogic authenticationLogic;
	private OperationUserStore operationUserStore;

	private CxfSessions cxfSessions;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		tokenGenerator = mock(TokenGenerator.class);
		sessionStore = mock(SessionStore.class);
		authenticationLogic = mock(AuthenticationLogic.class);
		operationUserStore = mock(OperationUserStore.class);
		cxfSessions = new CxfSessions(errorHandler, tokenGenerator, sessionStore, authenticationLogic,
				operationUserStore);
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
	public void create_errorOnAuthenticationLogicPropagated() throws Exception {
		// given
		final Session session = newSession() //
				.withUsername("foo") //
				.withPassword("bar") //
				.build();
		doThrow(AuthExceptionType.AUTH_LOGIN_WRONG.createException()) //
				.when(authenticationLogic).login(any(LoginDTO.class), any(UserStore.class));

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
		when(authenticationLogic.login(any(LoginDTO.class), any(UserStore.class))).thenReturn(null);
		doReturn("token") //
				.when(tokenGenerator).generate(anyString());

		// when
		final ResponseSingle<String> response = cxfSessions.create(session);

		// then
		final ArgumentCaptor<LoginDTO> loginCaptor = ArgumentCaptor.forClass(LoginDTO.class);
		verify(authenticationLogic).login(loginCaptor.capture(), any(UserStore.class));
		verify(tokenGenerator).generate(eq(session.getUsername()));
		verify(sessionStore).put(eq(newSession(session) //
				.withId("token") //
				.build()));
		final ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
		verify(operationUserStore).put(sessionCaptor.capture(), any(OperationUser.class));
		verifyNoMoreInteractions(errorHandler, tokenGenerator, sessionStore, authenticationLogic, operationUserStore);

		final LoginDTO capturedLogin = loginCaptor.getValue();
		assertThat(capturedLogin, equalTo(LoginDTO.newInstance() //
				.withLoginString(session.getUsername()) //
				.withPassword(session.getPassword()) //
				.build()));

		final Session capturedSession = sessionCaptor.getValue();
		assertThat(capturedSession, equalTo(newSession() //
				.withId("token") //
				.withUsername("username") //
				.withPassword("password") //
				.build()));

		assertThat(response.getElement(), equalTo("token"));
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
				.withGroup("group") //
				.build();
		doReturn(Optional.of(session)) //
				.when(sessionStore).get(anyString());

		// when
		final ResponseSingle<Session> response = cxfSessions.read("token");

		// then
		verify(sessionStore).get(eq("token"));
		verifyNoMoreInteractions(errorHandler, tokenGenerator, sessionStore, authenticationLogic, operationUserStore);
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
				.withGroup("group") //
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
		doReturn(Optional.of(session)) //
				.when(sessionStore).get(eq("token"));
		doReturn(Optional.absent()) //
				.when(operationUserStore).get(eq(session));
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
		doReturn(Optional.of(session)) //
				.when(sessionStore).get(eq("token"));
		final OperationUser operationUser = new OperationUser(mock(AuthenticatedUser.class),
				mock(PrivilegeContext.class), mock(CMGroup.class));
		doReturn(Optional.of(operationUser)) //
				.when(operationUserStore).get(eq(session));
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
				.withGroup("old group") //
				.build();
		final Session newSession = newSession() //
				.withId("new token") //
				.withGroup("new group") //
				.build();
		doReturn(Optional.of(oldSession)) //
				.when(sessionStore).get(anyString());
		final CMGroup group = mock(CMGroup.class);
		doReturn("guessed group").when(group).getName();
		doReturn(true).when(group).isActive();
		final OperationUser operationUser = new OperationUser(mock(AuthenticatedUser.class),
				mock(PrivilegeContext.class), group);
		doReturn(Optional.of(operationUser)) //
				.when(operationUserStore).get(eq(oldSession));

		// when
		cxfSessions.update("token", newSession);

		// then
		verify(sessionStore).get(eq("token"));
		verify(operationUserStore).get(eq(oldSession));
		verify(authenticationLogic).login( //
				eq(LoginDTO.newInstance() //
						.withLoginString(oldSession.getUsername()) //
						.withPassword(oldSession.getPassword()) //
						.withGroupName(newSession.getGroup()) //
						.build()), //
				any(UserStore.class));
		verify(sessionStore).put(eq(newSession(oldSession) //
				.withGroup("guessed group") //
				.build()));
		verify(operationUserStore).put( //
				eq(newSession(oldSession) //
						.withGroup("guessed group") //
						.build()), //
				eq(operationUser));
		verifyNoMoreInteractions(errorHandler, tokenGenerator, sessionStore, authenticationLogic, operationUserStore);
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
				.withGroup("group") //
				.build();
		doReturn(Optional.of(session)) //
				.when(sessionStore).get(anyString());
		final OperationUser operationUser = new OperationUser(mock(AuthenticatedUser.class),
				mock(PrivilegeContext.class), mock(CMGroup.class));
		doReturn(Optional.of(operationUser)) //
				.when(operationUserStore).get(any(Session.class));

		// when
		cxfSessions.delete("token");

		// then
		verify(sessionStore).get(eq("token"));
		verify(operationUserStore).get(eq(session));
		verify(sessionStore).remove(eq("token"));
		verify(operationUserStore).remove(eq(session));
		verifyNoMoreInteractions(errorHandler, tokenGenerator, sessionStore, authenticationLogic, operationUserStore);
	}

}
