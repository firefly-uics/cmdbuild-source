package org.cmdbuild.auth;

import org.cmdbuild.auth.PasswordAuthenticator.PasswordChanger;
import org.cmdbuild.auth.user.CMUser;
import org.apache.commons.lang.Validate;
import org.cmdbuild.auth.ClientRequestAuthenticator.ClientRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.cmdbuild.auth.AuthenticatedUser.ANONYMOUS_USER;

@Component
public class AuthenticationService {

	public static class ClientAuthenticatorResponse {

		private final AuthenticatedUser user;
		private final String redirectUrl;

		public static final ClientAuthenticatorResponse EMTPY_RESPONSE = new ClientAuthenticatorResponse(ANONYMOUS_USER, null);

		private ClientAuthenticatorResponse(final AuthenticatedUser user, final String redirectUrl) {
			Validate.notNull(user);
			this.user = user;
			this.redirectUrl = redirectUrl;
		}

		public final AuthenticatedUser getUser() {
			return user;
		}

		public final String getRedirectUrl() {
			return redirectUrl;
		}
	}

	public interface PasswordCallback {

		void setPassword(String password);
	}

	private interface FetchCallback {

		void foundUser(AuthenticatedUser authUser);
	}

	private static final PasswordAuthenticator[] NO_PASSWORD_AUTHENTICATORS = new PasswordAuthenticator[0];
	private static final ClientRequestAuthenticator[] NO_CLIENTREQUEST_AUTHENTICATORS = new ClientRequestAuthenticator[0];
	private static final UserFetcher[] NO_USER_FETCHERS = new UserFetcher[0];
	private static final UserStore DUMB_STORE = new UserStore() {

		@Override
		public AuthenticatedUser getUser() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void setUser(AuthenticatedUser user) {
			throw new UnsupportedOperationException("Not supported yet.");
		}
	};

	private PasswordAuthenticator[] passwordAuthenticators;
	private ClientRequestAuthenticator[] clientRequestAuthenticators;
	private UserFetcher[] userFetchers;
	private UserStore userStore;

	@Autowired
	public AuthenticationService() {
		passwordAuthenticators = NO_PASSWORD_AUTHENTICATORS;
		clientRequestAuthenticators = NO_CLIENTREQUEST_AUTHENTICATORS;
		userFetchers = NO_USER_FETCHERS;
		userStore = DUMB_STORE;
	}

	public void setPasswordAuthenticators(final PasswordAuthenticator ... passwordAuthenticators) {
		Validate.noNullElements(passwordAuthenticators);
		this.passwordAuthenticators = passwordAuthenticators;
	}

	public void setClientRequestAuthenticators(final ClientRequestAuthenticator ... clientRequestAuthenticators) {
		Validate.noNullElements(clientRequestAuthenticators);
		this.clientRequestAuthenticators = clientRequestAuthenticators;
	}

	public void setUserFetchers(final UserFetcher ... userFetchers) {
		Validate.noNullElements(userFetchers);
		this.userFetchers = userFetchers;
	}

	public void setUserStore(final UserStore userStore) {
		Validate.notNull(userStore);
		this.userStore = userStore;
	}

	/**
	 * Actively checks the user credentials and returns the authenticated
	 * user on success.
	 *
	 * @param login
	 * @param password unencrypted password
	 * @return the user that was authenticated
	 */
	public AuthenticatedUser authenticate(final Login login, final String password) {
		for (final PasswordAuthenticator pa : passwordAuthenticators) {
			if (pa.checkPassword(login, password)) {
				return fetchAuthenticatedUser(login, new FetchCallback() {

					@Override
					public void foundUser(final AuthenticatedUser authUser) {
						final PasswordChanger passwordChanger = pa.getPasswordChanger(login);
						authUser.setPasswordChanger(passwordChanger);
						userStore.setUser(authUser);
					}
				});
			}
		}
		return ANONYMOUS_USER;
	}

	/**
	 * Extracts the unencrypted password for the user and sets it in the
	 * {@param passwordCallback} for further processing.
	 *
	 * @param login
	 * @param passwordCallback object where to set the unencrypted password
	 * @return the user to be authenticated as if the authentication succeeded
	 */
	public AuthenticatedUser authenticate(final Login login, final PasswordCallback passwordCallback) {
		for (final PasswordAuthenticator pa : passwordAuthenticators) {
			final String pass = pa.fetchUnencryptedPassword(login);
			if (pass != null) {
				return fetchAuthenticatedUser(login, new FetchCallback() {

					@Override
					public void foundUser(final AuthenticatedUser authUser) {
						final PasswordChanger passwordChanger = pa.getPasswordChanger(login);
						authUser.setPasswordChanger(passwordChanger);
						userStore.setUser(authUser);
						passwordCallback.setPassword(pass);
					}
				});
			}
		}
		return ANONYMOUS_USER;
	}

	/**
	 * Tries to authenticate the user with a ClientRequestAuthenticator
	 *
	 * @param request object representing a client request
	 * @return response object with the authenticated user or a redirect URL
	 */
	public ClientAuthenticatorResponse authenticate(final ClientRequest request) {
		for (ClientRequestAuthenticator cra : clientRequestAuthenticators) {
			ClientRequestAuthenticator.Response response = cra.authenticate(request);
			if (response != null) {
				final AuthenticatedUser authUser = fetchAuthenticatedUser(response.getLogin(), new FetchCallback() {
					@Override
					public void foundUser(final AuthenticatedUser authUser) {
						userStore.setUser(authUser);
					}
				});
				return new ClientAuthenticatorResponse(authUser, response.getRedirectUrl());
			}
		}
		return ClientAuthenticatorResponse.EMTPY_RESPONSE;
	}

	/**
	 * Impersonate another user if the currently authenticated user has
	 * the right privileges.
	 *
	 * @param login
	 * @return the authenticated user
	 */
	public AuthenticatedUser impersonate(final Login login) {
//		final AuthenticatedUser authUser = userStore.getUser();
//		final CMUser user = fetchUser(login);
//		authUser.impersonate(user);
//		return authUser;
		throw new UnsupportedOperationException("Not implemented");
	}



	private AuthenticatedUser fetchAuthenticatedUser(final Login login, final FetchCallback callback) {
		AuthenticatedUser authUser = ANONYMOUS_USER;
		final CMUser user = fetchUser(login);
		if (user != null) {
			authUser = AuthenticatedUser.newInstance(user);
			callback.foundUser(authUser);
		}
		return authUser;
	}

	private CMUser fetchUser(final Login login) {
		CMUser user = null;
		for (UserFetcher uf : userFetchers) {
			user = uf.fetchUser(login);
			if (user != null) {
				break;
			}
		}
		return user;
	}
}
