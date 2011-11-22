package unit.auth;

import static org.cmdbuild.auth.AuthenticatedUser.ANONYMOUS_USER;
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

import org.junit.Test;

public class AuthenticationServiceTest {

	private static final Login LOGIN = Login.newInstance("Any User");
	private static final Login WRONG_LOGIN = Login.newInstance("Inexistent User");
	private static final String PASSWORD = "cleartext password";
	private static final String WRONG_PASSWORD = "wrong password";
	private final PasswordAuthenticator passwordAuthenticatorMock = mock(PasswordAuthenticator.class);
	private final ClientRequestAuthenticator clientRequestAuthenticatorMock = mock(ClientRequestAuthenticator.class);
	private final UserFetcher userFectcherMock = mock(UserFetcher.class);
	private final UserStore userStoreMock = mock(UserStore.class);
	private final CMUser user = mock(CMUser.class);

	/*
	 * ConstructorTest
	 */

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void passwordAuthenticatorsMustBeNotNull() {
		AuthenticationService as = new AuthenticationService();
		as.setPasswordAuthenticators((PasswordAuthenticator[]) null);
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void passwordAuthenticatorsMustHaveNotNullElements() {
		AuthenticationService as = new AuthenticationService();
		as.setPasswordAuthenticators(new PasswordAuthenticator[]{null});
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void clientRequestAuthenticatorsMustBeNotNull() {
		AuthenticationService as = new AuthenticationService();
		as.setClientRequestAuthenticators((ClientRequestAuthenticator[]) null);
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void clientRequestAuthenticatorsMustHaveNotNullElements() {
		AuthenticationService as = new AuthenticationService();
		as.setClientRequestAuthenticators(new ClientRequestAuthenticator[]{null});
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void userFetchersMustBeNotNull() {
		AuthenticationService as = new AuthenticationService();
		as.setUserFetchers((UserFetcher[]) null);
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void userFetchersMustHaveNotNullElements() {
		AuthenticationService as = new AuthenticationService();
		as.setUserFetchers(new UserFetcher[]{null});
	}

	@Test(expected = java.lang.IllegalArgumentException.class)
	public void userStoreMustBeNotNull() {
		AuthenticationService as = new AuthenticationService();
		as.setUserStore(null);
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

		verify(passwordAuthenticatorMock, times(1)).checkPassword(LOGIN, WRONG_PASSWORD);
		verify(passwordAuthenticatorMock, times(1)).checkPassword(WRONG_LOGIN, PASSWORD);
		verify(userFectcherMock, never()).fetchUser(any(Login.class));
		verify(passwordAuthenticatorMock, never()).getPasswordChanger(any(Login.class));
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
		when(userFectcherMock.fetchUser(LOGIN)).thenReturn(user);

		AuthenticatedUser authUser = as.authenticate(LOGIN, PASSWORD);
		assertThat(authUser, is(not((ANONYMOUS_USER))));

		verify(passwordAuthenticatorMock, times(1)).checkPassword(LOGIN, PASSWORD);
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
		when(userFectcherMock.fetchUser(LOGIN)).thenReturn(user);

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
		AuthenticationService as = new AuthenticationService();
		as.setUserStore(userStoreMock);
		return as;
	}

	private AuthenticationService mockedAuthenticatorService() {
		AuthenticationService as = new AuthenticationService();
		as.setPasswordAuthenticators(passwordAuthenticatorMock);
		as.setClientRequestAuthenticators(clientRequestAuthenticatorMock);
		as.setUserFetchers(userFectcherMock);
		as.setUserStore(userStoreMock);
		return as;
	}
}
