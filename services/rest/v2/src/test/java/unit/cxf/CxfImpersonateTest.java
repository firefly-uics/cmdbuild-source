package unit.cxf;

import static com.google.common.base.Predicates.alwaysFalse;
import static com.google.common.base.Predicates.alwaysTrue;
import static org.cmdbuild.service.rest.v2.model.Models.newSession;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.ws.rs.WebApplicationException;

import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.context.NullPrivilegeContext;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.logic.auth.LoginDTO;
import org.cmdbuild.service.rest.v2.cxf.CxfImpersonate;
import org.cmdbuild.service.rest.v2.cxf.CxfSessions.LoginHandler;
import org.cmdbuild.service.rest.v2.cxf.ErrorHandler;
import org.cmdbuild.service.rest.v2.cxf.service.OperationUserStore;
import org.cmdbuild.service.rest.v2.cxf.service.OperationUserStore.BySession;
import org.cmdbuild.service.rest.v2.cxf.service.SessionStore;
import org.cmdbuild.service.rest.v2.model.Session;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingObject;

public class CxfImpersonateTest {

	private static abstract class ForwardingPredicate<T> extends ForwardingObject implements Predicate<T> {

		/**
		 * Usable by subclasses only.
		 */
		protected ForwardingPredicate() {
		}

		@Override
		protected abstract Predicate<T> delegate();

		@Override
		public boolean apply(final T input) {
			return delegate().apply(input);
		}

	}

	private static class SettablePredicate<T> extends ForwardingPredicate<T> {

		private Predicate<T> delegate;

		public void setDelegate(final Predicate<T> delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Predicate<T> delegate() {
			return delegate;
		}

	}

	private static final Predicate<OperationUser> alwaysTrue = alwaysTrue();
	private static final Predicate<OperationUser> alwaysFalse = alwaysFalse();

	private ErrorHandler errorHandler;
	private LoginHandler loginHandler;
	private SessionStore sessionStore;
	private SessionStore impersonateSessionStore;
	private BySession operationUserStoreBySession;
	private OperationUserStore operationUserStore;
	private SettablePredicate<OperationUser> operationUserAllowed;

	private CxfImpersonate cxfImpersonate;

	@Before
	public void setUp() throws Exception {
		errorHandler = mock(ErrorHandler.class);
		loginHandler = mock(LoginHandler.class);
		sessionStore = mock(SessionStore.class);
		impersonateSessionStore = mock(SessionStore.class);
		operationUserStore = mock(OperationUserStore.class);
		operationUserStoreBySession = mock(BySession.class);
		doReturn(operationUserStoreBySession) //
				.when(operationUserStore).of(any(Session.class));
		operationUserAllowed = new SettablePredicate<OperationUser>();
		operationUserAllowed.setDelegate(alwaysTrue);

		cxfImpersonate = new CxfImpersonate(errorHandler, loginHandler, sessionStore, impersonateSessionStore,
				operationUserStore, operationUserAllowed);
	}

	@Test(expected = WebApplicationException.class)
	public void start_missingSessionThrowsException() throws Exception {
		// given
		doReturn(Optional.absent()) //
				.when(sessionStore).get(eq("token"));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).sessionNotFound(eq("token"));

		// when
		cxfImpersonate.start("token", "user");
	}

	@Test(expected = WebApplicationException.class)
	public void start_missingUserThrowsException() throws Exception {
		// given
		final Session session = newSession() //
				.withId("foo") //
				.build();
		doReturn(Optional.of(session)) //
				.when(sessionStore).get(eq("token"));
		doReturn(Optional.absent()) //
				.when(operationUserStoreBySession).get();
		doThrow(new WebApplicationException()) //
				.when(errorHandler).userNotFound(eq("token"));

		// when
		cxfImpersonate.start("token", "user");
	}

	@Test(expected = WebApplicationException.class)
	public void start_UserNotAllowedThrowsException() throws Exception {
		// given
		final Session session = newSession() //
				.withId("foo") //
				.build();
		doReturn(Optional.of(session)) //
				.when(sessionStore).get(eq("token"));
		final OperationUser operationUser = new OperationUser(mock(AuthenticatedUser.class),
				new NullPrivilegeContext(), new NullGroup());
		doReturn(Optional.of(operationUser)) //
				.when(operationUserStoreBySession).get();
		operationUserAllowed.setDelegate(alwaysFalse);
		doThrow(new WebApplicationException()) //
				.when(errorHandler).notAuthorized();

		// when
		cxfImpersonate.start("token", "user");
	}

	@Test
	public void start_sessionAndOperationUserReplaced() throws Exception {
		// given
		final Session session = newSession() //
				.withId("foo") //
				.build();
		doReturn(Optional.of(session)) //
				.when(sessionStore).get(eq("token"));
		final OperationUser actual = new OperationUser(mock(AuthenticatedUser.class), new NullPrivilegeContext(),
				new NullGroup());
		doReturn(Optional.of(actual)) //
				.when(operationUserStoreBySession).get();
		final OperationUser impersonated = new OperationUser(mock(AuthenticatedUser.class), new NullPrivilegeContext(),
				new NullGroup());
		doReturn(impersonated) //
				.when(loginHandler).login(any(LoginDTO.class));

		// when
		cxfImpersonate.start("token", "user");

		// then
		verify(sessionStore).get(eq("token"));
		verify(loginHandler).login(eq(LoginDTO.newInstance() //
				.withLoginString("user") //
				.withNoPasswordRequired() //
				.build()));
		verify(sessionStore).put(eq(newSession(session) //
				.withUsername("user") //
				.withAvailableRoles(impersonated.getAuthenticatedUser().getGroupNames()) //
				.build()));
		verify(impersonateSessionStore).put(eq(session));
		verify(operationUserStore, times(2)).of(eq(session));
		verify(operationUserStoreBySession).impersonate(eq(impersonated));
	}

	@Test(expected = WebApplicationException.class)
	public void stop_missingSessionThrowsException() throws Exception {
		// given
		doReturn(Optional.absent()) //
				.when(sessionStore).get(eq("token"));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).sessionNotFound(eq("token"));

		// when
		cxfImpersonate.stop("token");
	}

	@Test(expected = WebApplicationException.class)
	public void stop_missingPreviousSessionThrowsException() throws Exception {
		// given
		final Session session = newSession() //
				.withId("foo") //
				.build();
		doReturn(Optional.of(session)) //
				.when(sessionStore).get(eq("token"));
		doReturn(Optional.absent()) //
				.when(impersonateSessionStore).get(eq("token"));
		doThrow(new WebApplicationException()) //
				.when(errorHandler).sessionNotFound(eq("token"));

		// when
		cxfImpersonate.stop("token");
	}

	public void stop_sessionAndOperationUserRestored() throws Exception {
		// given
		final Session foo = newSession() //
				.withId("foo") //
				.build();
		doReturn(Optional.of(foo)) //
				.when(sessionStore).get(eq("token"));
		final Session bar = newSession() //
				.withId("bar") //
				.build();
		doReturn(Optional.of(bar)) //
				.when(impersonateSessionStore).get(eq("token"));

		// when
		cxfImpersonate.stop("token");

		// then
		verify(sessionStore).put(eq(bar));
		verify(impersonateSessionStore).remove(eq("foo"));
		verify(operationUserStore).of(eq(foo));
		verify(operationUserStoreBySession).impersonate(isNotNull(OperationUser.class));
	}

}
