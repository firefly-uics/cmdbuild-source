package unit.cxf.security;

import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.base.Predicates.not;
import static org.cmdbuild.auth.UserStores.inMemory;
import static org.cmdbuild.auth.user.AuthenticatedUserImpl.ANONYMOUS_USER;
import static org.cmdbuild.service.rest.v2.model.Models.newSession;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.context.NullPrivilegeContext;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.service.rest.v2.cxf.security.TokenHandler;
import org.cmdbuild.service.rest.v2.cxf.service.InMemoryOperationUserStore;
import org.cmdbuild.service.rest.v2.cxf.service.InMemorySessionStore;
import org.cmdbuild.service.rest.v2.cxf.service.InMemorySessionStore.Configuration;
import org.cmdbuild.service.rest.v2.cxf.service.OperationUserStore;
import org.cmdbuild.service.rest.v2.cxf.service.SessionStore;
import org.cmdbuild.service.rest.v2.model.Session;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class TokenHandlerTest {

	private static final ClassResourceInfo DUMMY_CLASS_RESOURCE_INFO = new ClassResourceInfo(TokenHandlerTest.class);

	private static final Predicate<Class<?>> IS_UNAUTHORIZED = alwaysTrue();
	private static final Predicate<Class<?>> IS_AUTHORIZED = not(IS_UNAUTHORIZED);

	private static final Optional<String> MISSING_TOKEN = Optional.absent();
	private static final Optional<String> TOKEN_FOO = Optional.of("foo");

	private SessionStore sessionStore;
	private OperationUserStore operationUserStore;
	private UserStore userStore;
	private org.cmdbuild.service.rest.v2.cxf.util.Messages.StringFromMessage tokenExtractor;

	@Before
	public void setUp() throws Exception {
		sessionStore = new InMemorySessionStore(new Configuration() {

			@Override
			public long timeout() {
				return 0L;
			}

		});
		operationUserStore = new InMemoryOperationUserStore();
		userStore = inMemory();
		tokenExtractor = mock(org.cmdbuild.service.rest.v2.cxf.util.Messages.StringFromMessage.class);
	}

	@Test
	public void nullResponseForUnauthorizedServiceWhenNoTokenReceiced() throws Exception {
		// given
		final TokenHandler tokenHandler = new TokenHandler(tokenExtractor, IS_UNAUTHORIZED, sessionStore,
				operationUserStore, userStore);
		doReturn(MISSING_TOKEN) //
				.when(tokenExtractor).apply(any(Message.class));
		final Message message = mock(Message.class);

		// when
		final Response response = tokenHandler.handleRequest(message, DUMMY_CLASS_RESOURCE_INFO);

		// then
		assertThat(response, equalTo(null));
		verifyZeroInteractions(tokenExtractor);
	}

	@Test
	public void nullResponseForUnauthorizedServiceWhenTokenReceiced() throws Exception {
		// given
		final TokenHandler tokenHandler = new TokenHandler(tokenExtractor, IS_UNAUTHORIZED, sessionStore,
				operationUserStore, userStore);
		doReturn(TOKEN_FOO) //
				.when(tokenExtractor).apply(any(Message.class));
		final Message message = mock(Message.class);

		// when
		final Response response = tokenHandler.handleRequest(message, DUMMY_CLASS_RESOURCE_INFO);

		// then
		assertThat(response, equalTo(null));
		verifyZeroInteractions(tokenExtractor);
	}

	@Test
	public void unauthorizedResponseForAuthorizedServiceWhenNoTokenReceiced() throws Exception {
		// given
		final TokenHandler tokenHandler = new TokenHandler(tokenExtractor, IS_AUTHORIZED, sessionStore,
				operationUserStore, userStore);
		doReturn(MISSING_TOKEN) //
				.when(tokenExtractor).apply(any(Message.class));
		final Message message = mock(Message.class);

		// when
		final Response response = tokenHandler.handleRequest(message, DUMMY_CLASS_RESOURCE_INFO);

		// then
		assertThat(response.getStatus(), equalTo(Status.UNAUTHORIZED.getStatusCode()));
		verify(tokenExtractor).apply(eq(message));
	}

	@Test
	public void unauthorizedResponseResponseForAuthorizedServiceWhenInvalidTokenReceiced() throws Exception {
		// given
		sessionStore.put(newSession().withId("bar").build());
		final TokenHandler tokenHandler = new TokenHandler(tokenExtractor, IS_AUTHORIZED, sessionStore,
				operationUserStore, userStore);
		doReturn(TOKEN_FOO) //
				.when(tokenExtractor).apply(any(Message.class));
		final Message message = mock(Message.class);

		// when
		final Response response = tokenHandler.handleRequest(message, DUMMY_CLASS_RESOURCE_INFO);

		// then
		assertThat(response.getStatus(), equalTo(Status.UNAUTHORIZED.getStatusCode()));
		verify(tokenExtractor).apply(eq(message));
	}

	@Test
	public void unauthorizedResponseForAuthorizedServiceWhenExistingTokenReceicedButMissingOperationUser()
			throws Exception {
		// given
		sessionStore.put(newSession().withId("foo").build());
		final TokenHandler tokenHandler = new TokenHandler(tokenExtractor, IS_AUTHORIZED, sessionStore,
				operationUserStore, userStore);
		doReturn(TOKEN_FOO) //
				.when(tokenExtractor).apply(any(Message.class));
		final Message message = mock(Message.class);

		// when
		final Response response = tokenHandler.handleRequest(message, DUMMY_CLASS_RESOURCE_INFO);

		// then
		assertThat(response.getStatus(), equalTo(Status.UNAUTHORIZED.getStatusCode()));
		verify(tokenExtractor).apply(eq(message));
	}

	@Test
	public void nullResponseForAuthorizedServiceWhenExistingTokenReceicedAndExistingOperationUser() throws Exception {
		// given
		final Session session = newSession().withId("foo").build();
		final OperationUser operationUser = new OperationUser(ANONYMOUS_USER, new NullPrivilegeContext(),
				new NullGroup());
		sessionStore.put(session);
		operationUserStore.of(session).main(operationUser);
		final TokenHandler tokenHandler = new TokenHandler(tokenExtractor, IS_AUTHORIZED, sessionStore,
				operationUserStore, userStore);
		doReturn(TOKEN_FOO) //
				.when(tokenExtractor).apply(any(Message.class));
		final Message message = mock(Message.class);

		// when
		final Response response = tokenHandler.handleRequest(message, DUMMY_CLASS_RESOURCE_INFO);

		// then
		assertThat(response, equalTo(null));
		assertThat(userStore.getUser(), equalTo(operationUser));
		verify(tokenExtractor).apply(eq(message));
	}

}
