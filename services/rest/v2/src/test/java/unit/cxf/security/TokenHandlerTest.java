package unit.cxf.security;

import static org.cmdbuild.auth.UserStores.inMemory;
import static org.cmdbuild.auth.user.AuthenticatedUserImpl.ANONYMOUS_USER;
import static org.cmdbuild.service.rest.v2.model.Models.newSession;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.context.NullPrivilegeContext;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.service.rest.v2.Unauthorized;
import org.cmdbuild.service.rest.v2.cxf.security.TokenHandler;
import org.cmdbuild.service.rest.v2.cxf.service.InMemoryOperationUserStore;
import org.cmdbuild.service.rest.v2.cxf.service.InMemorySessionStore;
import org.cmdbuild.service.rest.v2.cxf.service.InMemorySessionStore.Configuration;
import org.cmdbuild.service.rest.v2.cxf.service.OperationUserStore;
import org.cmdbuild.service.rest.v2.cxf.service.SessionStore;
import org.cmdbuild.service.rest.v2.cxf.util.Messages.StringFromMessage;
import org.cmdbuild.service.rest.v2.model.Session;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.common.base.Optional;

public class TokenHandlerTest {

	private static interface Dummy {

		void dummy();

	}

	private static class DummyUnauthorized implements Dummy {

		@Override
		@Unauthorized
		public void dummy() {
		}

	}

	private static class DummyAuthorized implements Dummy {

		@Override
		public void dummy() {
		}

	}

	private static final Optional<String> MISSING_TOKEN = Optional.absent();
	private static final Optional<String> TOKEN_FOO = Optional.of("foo");

	private SessionStore sessionStore;
	private OperationUserStore operationUserStore;
	private UserStore userStore;
	private StringFromMessage tokenExtractor;

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
		tokenExtractor = mock(StringFromMessage.class);
	}

	@Test
	public void unauthorizedServicesHaveNoOtherRequirements() throws Exception {
		// given
		final ResourceInfo resourceInfo = mock(ResourceInfo.class);
		doReturn(DummyUnauthorized.class.getMethod("dummy")) //
				.when(resourceInfo).getResourceMethod();
		final TokenHandler tokenHandler = new TokenHandler(tokenExtractor, sessionStore, operationUserStore, userStore,
				resourceInfo);
		final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);

		// when
		tokenHandler.filter(requestContext);

		// then
		verify(resourceInfo).getResourceMethod();
		verifyNoMoreInteractions(tokenExtractor, resourceInfo, requestContext);
	}

	@Test
	public void unauthorizedResponseForAuthorizedServiceWhenNoTokenReceiced() throws Exception {
		// given
		final ResourceInfo resourceInfo = mock(ResourceInfo.class);
		doReturn(DummyAuthorized.class.getMethod("dummy")) //
				.when(resourceInfo).getResourceMethod();
		final TokenHandler tokenHandler = new TokenHandler(tokenExtractor, sessionStore, operationUserStore, userStore,
				resourceInfo);
		doReturn(MISSING_TOKEN) //
				.when(tokenExtractor).apply(any(ContainerRequestContext.class));
		final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);

		// when
		tokenHandler.filter(requestContext);

		// then
		verify(resourceInfo).getResourceMethod();
		verify(tokenExtractor).apply(requestContext);
		final ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
		verify(requestContext).abortWith(captor.capture());
		verifyNoMoreInteractions(tokenExtractor, resourceInfo, requestContext);

		assertThat(captor.getValue().getStatus(), equalTo(Status.UNAUTHORIZED.getStatusCode()));
	}

	@Test
	public void unauthorizedResponseResponseForAuthorizedServiceWhenInvalidTokenReceiced() throws Exception {
		// given
		sessionStore.put(newSession().withId("bar").build());
		final ResourceInfo resourceInfo = mock(ResourceInfo.class);
		doReturn(DummyAuthorized.class.getMethod("dummy")) //
				.when(resourceInfo).getResourceMethod();
		final TokenHandler tokenHandler = new TokenHandler(tokenExtractor, sessionStore, operationUserStore, userStore,
				resourceInfo);
		doReturn(TOKEN_FOO) //
				.when(tokenExtractor).apply(any(ContainerRequestContext.class));
		final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);

		// when
		tokenHandler.filter(requestContext);

		// then
		verify(resourceInfo).getResourceMethod();
		verify(tokenExtractor).apply(requestContext);
		final ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
		verify(requestContext).abortWith(captor.capture());
		verifyNoMoreInteractions(tokenExtractor, resourceInfo, requestContext);

		assertThat(captor.getValue().getStatus(), equalTo(Status.UNAUTHORIZED.getStatusCode()));
	}

	@Test
	public void unauthorizedResponseForAuthorizedServiceWhenExistingTokenReceicedButMissingOperationUser()
			throws Exception {
		// given
		sessionStore.put(newSession().withId("foo").build());
		final ResourceInfo resourceInfo = mock(ResourceInfo.class);
		doReturn(DummyAuthorized.class.getMethod("dummy")) //
				.when(resourceInfo).getResourceMethod();
		final TokenHandler tokenHandler = new TokenHandler(tokenExtractor, sessionStore, operationUserStore, userStore,
				resourceInfo);
		doReturn(TOKEN_FOO) //
				.when(tokenExtractor).apply(any(ContainerRequestContext.class));
		final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);

		// when
		tokenHandler.filter(requestContext);

		// then
		verify(resourceInfo).getResourceMethod();
		verify(tokenExtractor).apply(requestContext);
		final ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
		verify(requestContext).abortWith(captor.capture());
		verifyNoMoreInteractions(tokenExtractor, resourceInfo, requestContext);

		assertThat(captor.getValue().getStatus(), equalTo(Status.UNAUTHORIZED.getStatusCode()));
	}

	@Test
	public void nullResponseForAuthorizedServiceWhenExistingTokenReceicedAndExistingOperationUser() throws Exception {
		// given
		final Session session = newSession().withId("foo").build();
		final OperationUser operationUser = new OperationUser(ANONYMOUS_USER, new NullPrivilegeContext(),
				new NullGroup());
		sessionStore.put(session);
		operationUserStore.of(session).main(operationUser);
		final ResourceInfo resourceInfo = mock(ResourceInfo.class);
		doReturn(DummyAuthorized.class.getMethod("dummy")) //
				.when(resourceInfo).getResourceMethod();
		final TokenHandler tokenHandler = new TokenHandler(tokenExtractor, sessionStore, operationUserStore, userStore,
				resourceInfo);
		doReturn(TOKEN_FOO) //
				.when(tokenExtractor).apply(any(ContainerRequestContext.class));
		final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);

		// when
		tokenHandler.filter(requestContext);

		// then
		verify(resourceInfo).getResourceMethod();
		verify(tokenExtractor).apply(requestContext);
		verifyNoMoreInteractions(tokenExtractor, resourceInfo, requestContext);

		assertThat(userStore.getUser(), equalTo(operationUser));
	}

}
