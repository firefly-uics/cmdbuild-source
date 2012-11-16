package unit.auth;

import static org.cmdbuild.auth.AuthenticatedUserImpl.ANONYMOUS_USER;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.cmdbuild.auth.AuthenticatedUser;
import org.cmdbuild.auth.AuthenticatedUserImpl;
import org.cmdbuild.auth.ClientRequestAuthenticator;
import org.cmdbuild.auth.ClientRequestAuthenticator.ClientRequest;
import org.cmdbuild.auth.ClientRequestAuthenticator.Response;
import org.cmdbuild.auth.DefaultAuthenticationService;
import org.cmdbuild.auth.DefaultAuthenticationService.ClientAuthenticatorResponse;
import org.cmdbuild.auth.DefaultAuthenticationService.Configuration;
import org.cmdbuild.auth.DefaultAuthenticationService.PasswordCallback;
import org.cmdbuild.auth.Login;
import org.cmdbuild.auth.PasswordAuthenticator;
import org.cmdbuild.auth.UserFetcher;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.user.CMUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationServiceTest {

	private static final Login LOGIN = Login.newInstance("Any User");
	private static final Login WRONG_LOGIN = Login.newInstance("Inexistent User");
	private static final String PASSWORD = "cleartext password";
	private static final String WRONG_PASSWORD = "wrong password";

	@Mock
	private PasswordAuthenticator passwordAuthenticatorMock;
	@Mock
	private ClientRequestAuthenticator clientRequestAuthenticatorMock;
	@Mock
	private UserFetcher userFectcherMock;
	@Mock
	private UserStore userStoreMock;
	@Mock
	private CMUser user;

	/*
	 * Constructor and setters
	 */

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void configurationMustBeNotNull() {
		final DefaultAuthenticationService as = new DefaultAuthenticationService(null);
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void passwordAuthenticatorsMustBeNotNull() {
		final DefaultAuthenticationService as = new DefaultAuthenticationService();
		as.setPasswordAuthenticators((PasswordAuthenticator[]) null);
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void passwordAuthenticatorsMustHaveNotNullElements() {
		final DefaultAuthenticationService as = new DefaultAuthenticationService();
		as.setPasswordAuthenticators(new PasswordAuthenticator[] { null });
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void clientRequestAuthenticatorsMustBeNotNull() {
		final DefaultAuthenticationService as = new DefaultAuthenticationService();
		as.setClientRequestAuthenticators((ClientRequestAuthenticator[]) null);
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void clientRequestAuthenticatorsMustHaveNotNullElements() {
		final DefaultAuthenticationService as = new DefaultAuthenticationService();
		as.setClientRequestAuthenticators(new ClientRequestAuthenticator[] { null });
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void userFetchersMustBeNotNull() {
		final DefaultAuthenticationService as = new DefaultAuthenticationService();
		as.setUserFetchers((UserFetcher[]) null);
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void userFetchersMustHaveNotNullElements() {
		final DefaultAuthenticationService as = new DefaultAuthenticationService();
		as.setUserFetchers(new UserFetcher[] { null });
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void userStoreMustBeNotNull() {
		final DefaultAuthenticationService as = new DefaultAuthenticationService();
		as.setUserStore(null);
	}

	/*
	 * Login and password
	 */

	@Test
	public void passwordAuthReturnsAnonymousUserIfNoAuthenticatorIsDefined() {
		final DefaultAuthenticationService as = emptyAuthenticatorService();

		assertThat(as.authenticate(LOGIN, PASSWORD), is(anonymousUser()));
		assertThat(as.authenticate(LOGIN, WRONG_PASSWORD), is(anonymousUser()));
		verify(userStoreMock, never()).setUser(any(AuthenticatedUserImpl.class));
	}

	@Test
	public void passwordAuthReturnsAnonymousUserOnAuthFailure() {
		final DefaultAuthenticationService as = mockedAuthenticatorService();

		when(passwordAuthenticatorMock.checkPassword(LOGIN, PASSWORD)).thenReturn(Boolean.TRUE);

		assertThat(as.authenticate(LOGIN, WRONG_PASSWORD), is(anonymousUser()));
		assertThat(as.authenticate(WRONG_LOGIN, PASSWORD), is(anonymousUser()));

		verify(passwordAuthenticatorMock, times(1)).checkPassword(LOGIN, WRONG_PASSWORD);
		verify(passwordAuthenticatorMock, times(1)).checkPassword(WRONG_LOGIN, PASSWORD);
		verify(userFectcherMock, never()).fetchUser(any(Login.class));
		verify(passwordAuthenticatorMock, never()).getPasswordChanger(any(Login.class));
		verify(userStoreMock, never()).setUser(any(AuthenticatedUserImpl.class));
	}

	@Test
	public void passwordAuthReturnsAnonymousUserOnUserFetchFailure() {
		final DefaultAuthenticationService as = mockedAuthenticatorService();

		when(passwordAuthenticatorMock.checkPassword(LOGIN, PASSWORD)).thenReturn(Boolean.TRUE);

		final AuthenticatedUser authUser = as.authenticate(LOGIN, PASSWORD);
		assertThat(authUser, is(anonymousUser()));

		verify(passwordAuthenticatorMock, only()).checkPassword(any(Login.class), anyString());
		verify(userFectcherMock, only()).fetchUser(any(Login.class));
		verify(userStoreMock, never()).setUser(any(AuthenticatedUserImpl.class));
	}

	@Test
	public void passwordAuthReturnsAnAuthenticatedUserOnSuccess() {
		final DefaultAuthenticationService as = mockedAuthenticatorService();

		when(passwordAuthenticatorMock.checkPassword(LOGIN, PASSWORD)).thenReturn(Boolean.TRUE);
		when(userFectcherMock.fetchUser(LOGIN)).thenReturn(user);

		final AuthenticatedUser authUser = as.authenticate(LOGIN, PASSWORD);
		assertThat(authUser, is(not(anonymousUser())));

		verify(passwordAuthenticatorMock, times(1)).checkPassword(LOGIN, PASSWORD);
		verify(userFectcherMock, only()).fetchUser(LOGIN);
		verify(userStoreMock, only()).setUser(authUser);
	}

	/*
	 * Login and PasswordCallback
	 */

	@Test
	public void passwordCallbackAuthDoesNothingIfNoAuthenticatorIsDefined() {
		final DefaultAuthenticationService as = emptyAuthenticatorService();
		final PasswordCallback pwc = mock(PasswordCallback.class);

		as.authenticate(LOGIN, pwc);

		verify(pwc, never()).setPassword(anyString());
		verify(userStoreMock, never()).setUser(any(AuthenticatedUser.class));
	}

	@Test
	public void passwordCallbackAuthDoesNotSetCallbackObjectPasswordOnAuthFailure() {
		final DefaultAuthenticationService as = mockedAuthenticatorService();
		final PasswordCallback pwc = mock(PasswordCallback.class);

		as.authenticate(WRONG_LOGIN, pwc);

		verify(pwc, never()).setPassword(anyString());
		verify(passwordAuthenticatorMock, only()).fetchUnencryptedPassword(WRONG_LOGIN);
		verify(userFectcherMock, never()).fetchUser(any(Login.class));
		verify(userStoreMock, never()).setUser(any(AuthenticatedUser.class));
	}

	@Test
	public void passwordCallbackAuthDoesNotSetCallbackObjectPasswordOnFetchFailure() {
		final DefaultAuthenticationService as = mockedAuthenticatorService();
		final PasswordCallback pwc = mock(PasswordCallback.class);

		when(passwordAuthenticatorMock.fetchUnencryptedPassword(LOGIN)).thenReturn(PASSWORD);

		as.authenticate(LOGIN, pwc);

		verify(pwc, never()).setPassword(anyString());
		verify(passwordAuthenticatorMock, only()).fetchUnencryptedPassword(LOGIN);
		verify(userFectcherMock, only()).fetchUser(LOGIN);
		verify(userStoreMock, never()).setUser(any(AuthenticatedUser.class));
	}

	@Test
	public void passwordCallbackAuthSetsCallbackObjectPasswordOnSuccess() {
		final DefaultAuthenticationService as = mockedAuthenticatorService();
		final PasswordCallback pwc = mock(PasswordCallback.class);

		when(passwordAuthenticatorMock.fetchUnencryptedPassword(LOGIN)).thenReturn(PASSWORD);
		when(userFectcherMock.fetchUser(LOGIN)).thenReturn(user);

		as.authenticate(LOGIN, pwc);

		verify(pwc, only()).setPassword(PASSWORD);
		verify(passwordAuthenticatorMock, times(1)).fetchUnencryptedPassword(LOGIN);
		verify(userFectcherMock, only()).fetchUser(LOGIN);
		verify(passwordAuthenticatorMock, times(1)).getPasswordChanger(LOGIN);
		verify(userStoreMock, only()).setUser(any(AuthenticatedUser.class));
	}

	/*
	 * ClientRequest
	 */

	@Test
	public void clientRequestAuthReturnsAnonymousUserIfNoAuthenticatorIsDefined() {
		final DefaultAuthenticationService as = emptyAuthenticatorService();
		final ClientRequest request = mock(ClientRequest.class);

		final ClientAuthenticatorResponse authResponse = as.authenticate(request);
		assertThat(authResponse.getUser(), is(anonymousUser()));

		verify(request, never()).getHeader(anyString());
		verify(request, never()).getParameter(anyString());
		verify(userStoreMock, never()).setUser(any(AuthenticatedUser.class));
	}

	@Test
	public void clientRequestAuthReturnsAnonymousUserOnAuthFailure() {
		final DefaultAuthenticationService as = mockedAuthenticatorService();
		final ClientRequest request = mock(ClientRequest.class);

		when(clientRequestAuthenticatorMock.authenticate(request)).thenReturn(null);

		final ClientAuthenticatorResponse authResponse = as.authenticate(request);
		assertThat(authResponse.getUser(), is(anonymousUser()));

		verify(clientRequestAuthenticatorMock, only()).authenticate(request);
		verify(userFectcherMock, never()).fetchUser(any(Login.class));
		verify(userStoreMock, never()).setUser(any(AuthenticatedUser.class));
	}

	@Test
	public void clientRequestAuthReturnsAnonymousUserOnUserFetchFailure() {
		final DefaultAuthenticationService as = mockedAuthenticatorService();
		final ClientRequest request = mock(ClientRequest.class);

		when(clientRequestAuthenticatorMock.authenticate(request)).thenReturn(Response.newLoginResponse(LOGIN));

		final ClientAuthenticatorResponse authResponse = as.authenticate(request);
		assertThat(authResponse.getUser(), is(anonymousUser()));

		verify(clientRequestAuthenticatorMock, only()).authenticate(request);
		verify(userFectcherMock, only()).fetchUser(LOGIN);
		verify(userStoreMock, never()).setUser(any(AuthenticatedUser.class));
	}

	@Test
	public void clientRequestAuthReturnsAnAuthenticatedUserOnSuccess() {
		final DefaultAuthenticationService as = mockedAuthenticatorService();
		final ClientRequest request = mock(ClientRequest.class);

		when(clientRequestAuthenticatorMock.authenticate(request)).thenReturn(Response.newLoginResponse(LOGIN));
		when(userFectcherMock.fetchUser(LOGIN)).thenReturn(user);

		final ClientAuthenticatorResponse authResponse = as.authenticate(request);
		assertThat(authResponse.getUser(), is(not(anonymousUser())));

		verify(clientRequestAuthenticatorMock, only()).authenticate(request);
		verify(userFectcherMock, only()).fetchUser(LOGIN);
		verify(userStoreMock, only()).setUser(any(AuthenticatedUser.class));
	}

	@Test
	public void clientRequestAuthCopiesTheAuthenticatorRedirectUrl() {
		final DefaultAuthenticationService as = mockedAuthenticatorService();
		final ClientRequest request = mock(ClientRequest.class);
		final String redirectUrl = "http://www.example.com/";

		when(clientRequestAuthenticatorMock.authenticate(request))
				.thenReturn(Response.newRedirectResponse(redirectUrl));

		final ClientAuthenticatorResponse authResponse = as.authenticate(request);
		assertThat(authResponse.getRedirectUrl(), is(redirectUrl));
	}

	/*
	 * Configuration
	 */

	@Test
	public void configurationFiltersPasswordAuthenticators() {
		final PasswordAuthenticator namedAuthenticatorMock = mock(PasswordAuthenticator.class, withSettings().name("b"));
		when(namedAuthenticatorMock.getName()).thenReturn("a");

		final Configuration conf = mock(Configuration.class);
		when(conf.getActiveAuthenticators()).thenReturn(Sets.newHashSet("a"));

		final DefaultAuthenticationService as = new DefaultAuthenticationService(conf);
		as.setPasswordAuthenticators(passwordAuthenticatorMock, namedAuthenticatorMock);
		as.authenticate(LOGIN, PASSWORD);

		verify(passwordAuthenticatorMock, never()).checkPassword(any(Login.class), anyString());
		verify(namedAuthenticatorMock, times(1)).checkPassword(any(Login.class), anyString());
	}

	@Test
	public void configurationFiltersPasswordCallbackAuthenticators() {
		final PasswordAuthenticator namedAuthenticatorMock = mock(PasswordAuthenticator.class, withSettings().name("b"));
		when(namedAuthenticatorMock.getName()).thenReturn("a");

		final Configuration conf = mock(Configuration.class);
		when(conf.getActiveAuthenticators()).thenReturn(Sets.newHashSet("a"));

		final DefaultAuthenticationService as = new DefaultAuthenticationService(conf);
		as.setPasswordAuthenticators(passwordAuthenticatorMock, namedAuthenticatorMock);
		as.authenticate(LOGIN, mock(PasswordCallback.class));

		verify(passwordAuthenticatorMock, never()).fetchUnencryptedPassword(any(Login.class));
		verify(namedAuthenticatorMock, times(1)).fetchUnencryptedPassword(any(Login.class));
	}

	@Test
	public void configurationFiltersClientRequestAuthenticators() {
		final Configuration conf = mock(Configuration.class);
		when(conf.getActiveAuthenticators()).thenReturn(Sets.newHashSet("a"));

		final ClientRequestAuthenticator namedAuthenticatorMock = mock(ClientRequestAuthenticator.class);
		when(namedAuthenticatorMock.getName()).thenReturn("a");

		final DefaultAuthenticationService as = new DefaultAuthenticationService(conf);
		as.setClientRequestAuthenticators(clientRequestAuthenticatorMock, namedAuthenticatorMock);
		as.authenticate(mock(ClientRequest.class));

		verify(clientRequestAuthenticatorMock, never()).authenticate(any(ClientRequest.class));
		verify(namedAuthenticatorMock, times(1)).authenticate(any(ClientRequest.class));
	}

	@Test
	public void serviceUsersCannotUseLoginPasswordAuthentication() {
		final Configuration conf = mock(Configuration.class);
		when(conf.getActiveAuthenticators()).thenReturn(null);
		when(conf.getServiceUsers()).thenReturn(Sets.newHashSet(LOGIN.getValue()));

		final DefaultAuthenticationService as = new DefaultAuthenticationService(conf);
		as.setPasswordAuthenticators(passwordAuthenticatorMock);

		as.authenticate(LOGIN, PASSWORD);
		as.authenticate(LOGIN, mock(PasswordCallback.class));

		verify(passwordAuthenticatorMock, never()).checkPassword(LOGIN, PASSWORD);
		verify(passwordAuthenticatorMock, times(1)).fetchUnencryptedPassword(LOGIN);
	}

	/*
	 * Impersonate
	 */

	@Test
	public void impersonateIsAllowedOnlyToAdministratorsAndServiceUsers() {
		final Configuration conf = mock(Configuration.class);
		when(conf.getServiceUsers()).thenReturn(Sets.newHashSet("service"));
		final DefaultAuthenticationService as = mockedAuthenticatorService(conf);

		final AuthenticatedUser authUserMock = mock(AuthenticatedUser.class);
		when(userStoreMock.getUser()).thenReturn(authUserMock);
		when(userFectcherMock.fetchUser(LOGIN)).thenReturn(user);

		when(authUserMock.hasAdministratorPrivileges()).thenReturn(true);
		as.impersonate(LOGIN);

		reset(authUserMock);
		when(authUserMock.getName()).thenReturn("service");
		as.impersonate(LOGIN);

		reset(authUserMock);
		try {
			as.impersonate(LOGIN);
			fail("Should have thrown");
		} catch (final UnsupportedOperationException e) {
			// Should throw
		}
	}

	@Test
	public void anExistingUserCanBeImpersonated() {
		final DefaultAuthenticationService as = mockedAuthenticatorService();
		final AuthenticatedUser authUserMock = mock(AuthenticatedUser.class);
		when(authUserMock.hasAdministratorPrivileges()).thenReturn(true);
		when(userStoreMock.getUser()).thenReturn(authUserMock);
		when(userFectcherMock.fetchUser(LOGIN)).thenReturn(user);

		assertThat(as.impersonate(LOGIN), is(authUserMock));

		verify(authUserMock, times(1)).impersonate(user);
	}

	/*
	 * Utility methods
	 */

	private DefaultAuthenticationService emptyAuthenticatorService() {
		final DefaultAuthenticationService as = new DefaultAuthenticationService();
		as.setUserStore(userStoreMock);
		return as;
	}

	private DefaultAuthenticationService mockedAuthenticatorService(final Configuration conf) {
		final DefaultAuthenticationService as = new DefaultAuthenticationService(conf);
		setupMockedAuthenticationService(as);
		return as;
	}

	private DefaultAuthenticationService mockedAuthenticatorService() {
		final DefaultAuthenticationService as = new DefaultAuthenticationService();
		setupMockedAuthenticationService(as);
		return as;
	}

	private void setupMockedAuthenticationService(final DefaultAuthenticationService as) {
		as.setPasswordAuthenticators(passwordAuthenticatorMock);
		as.setClientRequestAuthenticators(clientRequestAuthenticatorMock);
		as.setUserFetchers(userFectcherMock);
		as.setUserStore(userStoreMock);
	}

	private AuthenticatedUser anonymousUser() {
		return ANONYMOUS_USER;
	}
}
