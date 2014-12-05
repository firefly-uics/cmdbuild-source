package unit.cxf.security;

import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.base.Predicates.not;
import static java.util.Arrays.asList;
import static org.apache.cxf.message.Message.PROTOCOL_HEADERS;
import static org.cmdbuild.auth.UserStores.inMemory;
import static org.cmdbuild.auth.user.AuthenticatedUserImpl.ANONYMOUS_USER;
import static org.cmdbuild.service.rest.cxf.security.TokenHandler.TOKEN_HEADER;
import static org.cmdbuild.service.rest.model.Models.newSession;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.context.NullPrivilegeContext;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.service.rest.cxf.security.TokenHandler;
import org.cmdbuild.service.rest.cxf.service.InMemoryOperationUserStore;
import org.cmdbuild.service.rest.cxf.service.InMemorySessionStore;
import org.cmdbuild.service.rest.cxf.service.OperationUserStore;
import org.cmdbuild.service.rest.cxf.service.SessionStore;
import org.cmdbuild.service.rest.model.Session;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Predicate;

public class TokenHandlerTest {

	private static final Map<String, List<String>> EMPTY_HEADERS = Collections.emptyMap();

	private static final ClassResourceInfo DUMMY_CLASS_RESOURCE_INFO = new ClassResourceInfo(TokenHandlerTest.class);

	private static final Predicate<Class<?>> IS_UNAUTHORIZED = alwaysTrue();
	private static final Predicate<Class<?>> IS_AUTHORIZED = not(IS_UNAUTHORIZED);

	private SessionStore sessionStore;
	private OperationUserStore operationUserStore;
	private UserStore userStore;

	@Before
	public void setUp() throws Exception {
		sessionStore = new InMemorySessionStore();
		operationUserStore = new InMemoryOperationUserStore();
		userStore = inMemory();
	}

	@Test
	public void NullResponseForUnauthorizedServiceWhenNoTokenReceiced() throws Exception {
		// given
		final TokenHandler tokenHandler = new TokenHandler(IS_UNAUTHORIZED, sessionStore, operationUserStore, userStore);
		final Message message = mock(Message.class);
		doReturn(EMPTY_HEADERS) //
				.when(message).get(anyString());

		// when
		final Response response = tokenHandler.handleRequest(message, DUMMY_CLASS_RESOURCE_INFO);

		// then
		verify(message).get(eq(PROTOCOL_HEADERS));
		assertThat(response, equalTo(null));
	}

	@Test
	public void NullResponseForUnauthorizedServiceWhenTokenReceiced() throws Exception {
		// given
		final TokenHandler tokenHandler = new TokenHandler(IS_UNAUTHORIZED, sessionStore, operationUserStore, userStore);
		final Message message = mock(Message.class);
		doReturn(headersWithToken("foo")) //
				.when(message).get(anyString());

		// when
		final Response response = tokenHandler.handleRequest(message, DUMMY_CLASS_RESOURCE_INFO);

		// then
		verify(message).get(eq(PROTOCOL_HEADERS));
		assertThat(response, equalTo(null));
	}

	@Test
	public void UnauthorizedResponseForAuthorizedServiceWhenNoTokenReceiced() throws Exception {
		// given
		final TokenHandler tokenHandler = new TokenHandler(IS_AUTHORIZED, sessionStore, operationUserStore, userStore);
		final Message message = mock(Message.class);
		doReturn(EMPTY_HEADERS) //
				.when(message).get(anyString());

		// when
		final Response response = tokenHandler.handleRequest(message, DUMMY_CLASS_RESOURCE_INFO);

		// then
		verify(message).get(eq(PROTOCOL_HEADERS));
		assertThat(response.getStatus(), equalTo(Status.UNAUTHORIZED.getStatusCode()));
	}

	@Test
	public void UnauthorizedResponseResponseForAuthorizedServiceWhenInvalidTokenReceiced() throws Exception {
		// given
		sessionStore.put(newSession().withId("bar").build());
		final TokenHandler tokenHandler = new TokenHandler(IS_AUTHORIZED, sessionStore, operationUserStore, userStore);
		final Message message = mock(Message.class);
		doReturn(headersWithToken("foo")) //
				.when(message).get(anyString());

		// when
		final Response response = tokenHandler.handleRequest(message, DUMMY_CLASS_RESOURCE_INFO);

		// then
		verify(message).get(eq(PROTOCOL_HEADERS));
		assertThat(response.getStatus(), equalTo(Status.UNAUTHORIZED.getStatusCode()));
	}

	@Test
	public void UnauthorizedResponseForAuthorizedServiceWhenExistingTokenReceicedButMissingOperationUser()
			throws Exception {
		// given
		sessionStore.put(newSession().withId("foo").build());
		final TokenHandler tokenHandler = new TokenHandler(IS_AUTHORIZED, sessionStore, operationUserStore, userStore);
		final Message message = mock(Message.class);
		doReturn(headersWithToken("foo")) //
				.when(message).get(anyString());

		// when
		final Response response = tokenHandler.handleRequest(message, DUMMY_CLASS_RESOURCE_INFO);

		// then
		verify(message).get(eq(PROTOCOL_HEADERS));
		assertThat(response.getStatus(), equalTo(Status.UNAUTHORIZED.getStatusCode()));
	}

	@Test
	public void NullResponseForAuthorizedServiceWhenExistingTokenReceicedAndExistingOperationUser() throws Exception {
		// given
		final Session session = newSession().withId("foo").build();
		final OperationUser operationUser = new OperationUser(ANONYMOUS_USER, new NullPrivilegeContext(),
				new NullGroup());
		sessionStore.put(session);
		operationUserStore.of(session).main(operationUser);
		final TokenHandler tokenHandler = new TokenHandler(IS_AUTHORIZED, sessionStore, operationUserStore, userStore);
		final Message message = mock(Message.class);
		doReturn(headersWithToken("foo")) //
				.when(message).get(anyString());

		// when
		final Response response = tokenHandler.handleRequest(message, DUMMY_CLASS_RESOURCE_INFO);

		// then
		verify(message).get(eq(PROTOCOL_HEADERS));
		assertThat(response, equalTo(null));
		assertThat(userStore.getUser(), equalTo(operationUser));
	}

	private Map<String, List<String>> headersWithToken(final String value) {
		return ChainablePutMap.of(new HashMap<String, List<String>>()) //
				.chainablePut(TOKEN_HEADER, asList(value));
	}

}
