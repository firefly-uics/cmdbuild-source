package org.cmdbuild.auth;

import org.cmdbuild.auth.Login.LoginType;
import java.util.Set;
import org.cmdbuild.auth.PasswordAuthenticator.PasswordChanger;
import org.cmdbuild.auth.user.CMUser;
import org.apache.commons.lang.Validate;
import org.cmdbuild.auth.ClientRequestAuthenticator.ClientRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.cmdbuild.auth.AuthenticatedUserImpl.ANONYMOUS_USER;

@Component
public class AuthenticationService {

	public interface Configuration {
		/**
		 * Returns the names of the authenticators that should be activated,
		 * or null if all authenticators should be activated.
		 *
		 * @return active authenticators or null
		 */
		Set<String> getActiveAuthenticators();

		/**
		 * Return the names of the service users. They can only log in
		 * with password callback and can impersonate other users. Null
		 * means that there is no service user defined.
		 *
		 * @return a list of service users or null
		 */
		Set<String> getServiceUsers();
	}

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

		void foundUser(AuthenticatedUserImpl authUser);
	}

	private static final UserStore DUMB_STORE = new UserStore() {

		@Override
		public AuthenticatedUser getUser() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setUser(AuthenticatedUser user) {
			throw new UnsupportedOperationException();
		}
	};

	private PasswordAuthenticator[] passwordAuthenticators;
	private ClientRequestAuthenticator[] clientRequestAuthenticators;
	private UserFetcher[] userFetchers;
	private UserStore userStore;

	private final Set<String> serviceUsers;
	private final Set<String> authenticatorNames;

	public AuthenticationService() {
		this(new Configuration() {

			@Override
			public Set<String> getActiveAuthenticators() {
				return null;
			}

			@Override
			public Set<String> getServiceUsers() {
				return null;
			}
		});
	}

	@Autowired
	public AuthenticationService(final Configuration conf) {
		Validate.notNull(conf);
		this.serviceUsers = conf.getServiceUsers();
		this.authenticatorNames = conf.getActiveAuthenticators();
		passwordAuthenticators = new PasswordAuthenticator[0];
		clientRequestAuthenticators = new ClientRequestAuthenticator[0];
		userFetchers = new UserFetcher[0];
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

	private boolean isInactive(final CMAuthenticator authenticator) {
		return authenticatorNames != null && !authenticatorNames.contains(authenticator.getName());
	}

	private boolean isServiceUser(final CMUser user) {
		return isServiceUser(user.getName());
	}

	private boolean isServiceUser(final Login login) {
		return (login.getType() == LoginType.USERNAME) && isServiceUser(login.getValue());
	}

	private boolean isServiceUser(final String username) {
		return serviceUsers != null && serviceUsers.contains(username);
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
		if (isServiceUser(login)) {
			return ANONYMOUS_USER;
		}
		for (final PasswordAuthenticator pa : passwordAuthenticators) {
			if (isInactive(pa)) {
				continue;
			}
			if (pa.checkPassword(login, password)) {
				return fetchAuthenticatedUser(login, new FetchCallback() {

					@Override
					public void foundUser(final AuthenticatedUserImpl authUser) {
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
			if (isInactive(pa)) {
				continue;
			}
			final String pass = pa.fetchUnencryptedPassword(login);
			if (pass != null) {
				return fetchAuthenticatedUser(login, new FetchCallback() {

					@Override
					public void foundUser(final AuthenticatedUserImpl authUser) {
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
			if (isInactive(cra)) {
				continue;
			}
			ClientRequestAuthenticator.Response response = cra.authenticate(request);
			if (response != null) {
				final AuthenticatedUser authUser = fetchAuthenticatedUser(response.getLogin(), new FetchCallback() {
					@Override
					public void foundUser(final AuthenticatedUserImpl authUser) {
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
		final AuthenticatedUser authUser = userStore.getUser();
		if (authUser.hasAdministratorPrivileges() || isServiceUser(authUser)) {
			final CMUser user = fetchUser(login);
			authUser.impersonate(user);
			return authUser;
		}
		throw new UnsupportedOperationException();
	}



	private AuthenticatedUser fetchAuthenticatedUser(final Login login, final FetchCallback callback) {
		AuthenticatedUserImpl authUser = ANONYMOUS_USER;
		final CMUser user = fetchUser(login);
		if (user != null) {
			authUser = AuthenticatedUserImpl.newInstance(user);
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
