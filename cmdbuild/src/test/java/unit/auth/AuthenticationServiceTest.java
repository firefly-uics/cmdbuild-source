package unit.auth;

import static org.cmdbuild.auth.user.AnonymousUser.ANONYMOUS_USER;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.*;

import org.cmdbuild.auth.*;
import org.cmdbuild.auth.AuthenticationService.ClientAuthenticatorResponse;
import org.cmdbuild.auth.user.*;
import org.cmdbuild.auth.AuthenticationService.PasswordCallback;
import org.cmdbuild.auth.ClientRequestAuthenticator.Response;
import org.cmdbuild.auth.ClientRequestAuthenticator.ClientRequest;
import org.cmdbuild.auth.user.UserImpl;

import org.junit.Test;

public class AuthenticationServiceTest {

	private static final Login LOGIN = Login.newInstance("Any User");
	private static final Login WRONG_LOGIN = Login.newInstance("Inexistent User");
	private static final String PASSWORD = "cleartext password";
	private static final String WRONG_PASSWORD = "wrong password";
	private static final CMUser USER = new UserImpl();
	private static final AuthenticatedUser AUTHENTICATED_USER = AuthenticatedUser.newInstance(USER);

	private static final PasswordAuthenticator[] NO_PASSWORD_AUTHENTICATORS = new PasswordAuthenticator[0];
	private static final ClientRequestAuthenticator[] NO_CLIENTREQUEST_AUTHENTICATORS = new ClientRequestAuthenticator[0];
	private static final UserFetcher[] NO_USER_FETCHERS = new UserFetcher[0];

	private final PasswordAuthenticator passwordAuthenticatorMock = mock(PasswordAuthenticator.class);
	private final ClientRequestAuthenticator clientRequestAuthenticatorMock = mock(ClientRequestAuthenticator.class);
	private final UserFetcher userFectcherMock = mock(UserFetcher.class);
	private final UserStore userStoreMock = mock(UserStore.class);

	/*
	 * ConstructorTest
	 */
	@Test(expected = java.lang.IllegalArgumentException.class)
	public void passwordAuthenticatorsMustBeNotNull() {
		AuthenticationService as = new AuthenticationService(
				null,
				NO_CLIENTREQUEST_AUTHENTICATORS,
				NO_USER_FETCHERS,
				userStoreMock);
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void passwordAuthenticatorsMustHaveNotNullElements() {
		AuthenticationService as = new AuthenticationService(
				new PasswordAuthenticator[]{null},
				NO_CLIENTREQUEST_AUTHENTICATORS,
				NO_USER_FETCHERS,
				userStoreMock);
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void clientRequestAuthenticatorsMustBeNotNull() {
		AuthenticationService as = new AuthenticationService(
				NO_PASSWORD_AUTHENTICATORS,
				null,
				NO_USER_FETCHERS,
				userStoreMock);
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void clientRequestAuthenticatorsMustHaveNotNullElements() {
		AuthenticationService as = new AuthenticationService(
				NO_PASSWORD_AUTHENTICATORS,
				new ClientRequestAuthenticator[]{null},
				NO_USER_FETCHERS,
				userStoreMock);
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void userFetchersMustBeNotNull() {
		AuthenticationService as = new AuthenticationService(
				NO_PASSWORD_AUTHENTICATORS,
				NO_CLIENTREQUEST_AUTHENTICATORS,
				null,
				userStoreMock);
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void userFetchersMustHaveNotNullElements() {
		AuthenticationService as = new AuthenticationService(
				NO_PASSWORD_AUTHENTICATORS,
				NO_CLIENTREQUEST_AUTHENTICATORS,
				new UserFetcher[]{null},
				userStoreMock);
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void userStoreMustBeNotNull() {
		AuthenticationService as = new AuthenticationService(
				NO_PASSWORD_AUTHENTICATORS,
				NO_CLIENTREQUEST_AUTHENTICATORS,
				NO_USER_FETCHERS,
				null);
	}

	/*
	 * Login and password
	 */
	@Test
	public void passwordAuthReturnsAnonymousUserIfNoAuthenticatorIsDefined() {
		AuthenticationService as = emptyAuthenticatorService();

		assertThat(as.authenticate(LOGIN, PASSWORD), is(ANONYMOUS_USER));
		assertThat(as.authenticate(LOGIN, WRONG_PASSWORD), is(ANONYMOUS_USER));
		verify(userStoreMock, never()).setUser(any(AuthenticatedUser.class));
	}

	@Test
	public void passwordAuthReturnsAnonymousUserOnAuthFailure() {
		AuthenticationService as = mockedAuthenticatorService();

		when(passwordAuthenticatorMock.checkPassword(LOGIN, PASSWORD)).thenReturn(Boolean.TRUE);

		assertThat(as.authenticate(LOGIN, WRONG_PASSWORD), is(ANONYMOUS_USER));
		assertThat(as.authenticate(WRONG_LOGIN, PASSWORD), is(ANONYMOUS_USER));

		verify(passwordAuthenticatorMock, times(2)).checkPassword(any(Login.class), anyString());
		verify(userFectcherMock, never()).fetchUser(any(Login.class));
		verify(userStoreMock, never()).setUser(any(AuthenticatedUser.class));
	}

	@Test
	public void passwordAuthReturnsAnonymousUserOnUserFetchFailure() {
		AuthenticationService as = mockedAuthenticatorService();

		when(passwordAuthenticatorMock.checkPassword(LOGIN, PASSWORD)).thenReturn(Boolean.TRUE);

		AuthenticatedUser authUser = as.authenticate(LOGIN, PASSWORD);
		assertThat(authUser, is(ANONYMOUS_USER));

		verify(passwordAuthenticatorMock, only()).checkPassword(any(Login.class), anyString());
		verify(userFectcherMock, only()).fetchUser(any(Login.class));
		verify(userStoreMock, never()).setUser(any(AuthenticatedUser.class));
	}

	@Test
	public void passwordAuthReturnsAnAuthenticatedUserOnSuccess() {
		AuthenticationService as = mockedAuthenticatorService();

		when(passwordAuthenticatorMock.checkPassword(LOGIN, PASSWORD)).thenReturn(Boolean.TRUE);
		when(userFectcherMock.fetchUser(LOGIN)).thenReturn(USER);

		AuthenticatedUser authUser = as.authenticate(LOGIN, PASSWORD);
		assertThat(authUser, is(not((ANONYMOUS_USER))));

		verify(passwordAuthenticatorMock, only()).checkPassword(LOGIN, PASSWORD);
		verify(userFectcherMock, only()).fetchUser(LOGIN);
		verify(userStoreMock, only()).setUser(authUser);
	}

	/*
	 * Login and PasswordCallback
	 */
	@Test
	public void passwordCallbackAuthDoesNothingIfNoAuthenticatorIsDefined() {
		AuthenticationService as = emptyAuthenticatorService();
		PasswordCallback pwc = mock(PasswordCallback.class);

		as.authenticate(LOGIN, pwc);

		verify(pwc, never()).setPassword(anyString());
		verify(userStoreMock, never()).setUser(any(AuthenticatedUser.class));
	}

	@Test
	public void passwordCallbackAuthDoesNotSetCallbackObjectPasswordOnAuthFailure() {
		AuthenticationService as = mockedAuthenticatorService();
		PasswordCallback pwc = mock(PasswordCallback.class);

		as.authenticate(WRONG_LOGIN, pwc);

		verify(pwc, never()).setPassword(anyString());
		verify(passwordAuthenticatorMock, only()).fetchUnencryptedPassword(WRONG_LOGIN);
		verify(userFectcherMock, never()).fetchUser(any(Login.class));
		verify(userStoreMock, never()).setUser(any(AuthenticatedUser.class));
	}

	@Test
	public void passwordCallbackAuthDoesNotSetCallbackObjectPasswordOnFetchFailure() {
		AuthenticationService as = mockedAuthenticatorService();
		PasswordCallback pwc = mock(PasswordCallback.class);

		when(passwordAuthenticatorMock.fetchUnencryptedPassword(LOGIN)).thenReturn(PASSWORD);

		as.authenticate(LOGIN, pwc);

		verify(pwc, never()).setPassword(anyString());
		verify(passwordAuthenticatorMock, only()).fetchUnencryptedPassword(LOGIN);
		verify(userFectcherMock, only()).fetchUser(LOGIN);
		verify(userStoreMock, never()).setUser(any(AuthenticatedUser.class));
	}

	@Test
	public void passwordCallbackAuthSetsCallbackObjectPasswordOnSuccess() {
		AuthenticationService as = mockedAuthenticatorService();
		PasswordCallback pwc = mock(PasswordCallback.class);

		when(passwordAuthenticatorMock.fetchUnencryptedPassword(LOGIN)).thenReturn(PASSWORD);
		when(userFectcherMock.fetchUser(LOGIN)).thenReturn(USER);

		as.authenticate(LOGIN, pwc);

		verify(pwc, only()).setPassword(PASSWORD);
		verify(passwordAuthenticatorMock, only()).fetchUnencryptedPassword(LOGIN);
		verify(userFectcherMock, only()).fetchUser(LOGIN);
		verify(userStoreMock, only()).setUser(any(AuthenticatedUser.class));
	}

	/*
	 * ClientRequest
	 */
	@Test
	public void clientRequestAuthReturnsAnonymousUserIfNoAuthenticatorIsDefined() {
		AuthenticationService as = emptyAuthenticatorService();
		ClientRequest request = mock(ClientRequest.class);

		ClientAuthenticatorResponse authResponse = as.authenticate(request);
		assertThat(authResponse.getUser(), is(ANONYMOUS_USER));

		verify(request, never()).getHeader(anyString());
		verify(request, never()).getParameter(anyString());
		verify(userStoreMock, never()).setUser(any(AuthenticatedUser.class));
	}

	@Test
	public void clientRequestAuthReturnsAnonymousUserOnAuthFailure() {
		AuthenticationService as = mockedAuthenticatorService();
		ClientRequest request = mock(ClientRequest.class);

		when(clientRequestAuthenticatorMock.authenticate(request)).thenReturn(null);

		ClientAuthenticatorResponse authResponse = as.authenticate(request);
		assertThat(authResponse.getUser(), is(ANONYMOUS_USER));

		verify(clientRequestAuthenticatorMock, only()).authenticate(request);
		verify(userFectcherMock, never()).fetchUser(any(Login.class));
		verify(userStoreMock, never()).setUser(any(AuthenticatedUser.class));
	}

	@Test
	public void clientRequestAuthReturnsAnonymousUserOnUserFetchFailure() {
		AuthenticationService as = mockedAuthenticatorService();
		ClientRequest request = mock(ClientRequest.class);

		when(clientRequestAuthenticatorMock.authenticate(request)).thenReturn(Response.newLoginResponse(LOGIN));

		ClientAuthenticatorResponse authResponse = as.authenticate(request);
		assertThat(authResponse.getUser(), is(ANONYMOUS_USER));

		verify(clientRequestAuthenticatorMock, only()).authenticate(request);
		verify(userFectcherMock, only()).fetchUser(LOGIN);
		verify(userStoreMock, never()).setUser(any(AuthenticatedUser.class));
	}

	@Test
	public void clientRequestAuthReturnsAnAuthenticatedUserOnSuccess() {
		AuthenticationService as = mockedAuthenticatorService();
		ClientRequest request = mock(ClientRequest.class);

		when(clientRequestAuthenticatorMock.authenticate(request)).thenReturn(Response.newLoginResponse(LOGIN));
		when(userFectcherMock.fetchUser(LOGIN)).thenReturn(USER);

		ClientAuthenticatorResponse authResponse = as.authenticate(request);
		assertThat(authResponse.getUser(), is(not(ANONYMOUS_USER)));

		verify(clientRequestAuthenticatorMock, only()).authenticate(request);
		verify(userFectcherMock, only()).fetchUser(LOGIN);
		verify(userStoreMock, only()).setUser(any(AuthenticatedUser.class));
	}

	@Test
	public void clientRequestAuthCopiesTheAuthenticatorRedirectUrl() {
		AuthenticationService as = mockedAuthenticatorService();
		ClientRequest request = mock(ClientRequest.class);
		String redirectUrl = "http://www.example.com/";

		when(clientRequestAuthenticatorMock.authenticate(request)).thenReturn(Response.newRedirectResponse(redirectUrl));

		ClientAuthenticatorResponse authResponse = as.authenticate(request);
		assertThat(authResponse.getRedirectUrl(), is(redirectUrl));
	}

	/*
	 * Utility methods
	 */
	private AuthenticationService emptyAuthenticatorService() {
		return new AuthenticationService(
				NO_PASSWORD_AUTHENTICATORS,
				NO_CLIENTREQUEST_AUTHENTICATORS,
				NO_USER_FETCHERS,
				userStoreMock);
	}

	private AuthenticationService mockedAuthenticatorService() {
		return new AuthenticationService(
				new PasswordAuthenticator[]{passwordAuthenticatorMock},
				new ClientRequestAuthenticator[]{clientRequestAuthenticatorMock},
				new UserFetcher[]{userFectcherMock},
				userStoreMock);
	}
}
