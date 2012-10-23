package org.cmdbuild.auth;

import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.apache.commons.lang.Validate;
import org.cmdbuild.auth.ClientRequestAuthenticator.ClientRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.cmdbuild.auth.user.AnonymousUser.ANONYMOUS_USER;

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

	private final PasswordAuthenticator[] passwordAuthenticators;
	private final ClientRequestAuthenticator[] clientRequestAuthenticators;
	private final UserFetcher[] userFetchers;
	private final UserStore userStore;

	// TODO: Too many parameters
	@Autowired
	public AuthenticationService(
			final PasswordAuthenticator[] passwordAuthenticators,
			final ClientRequestAuthenticator[] clientRequestAuthenticators,
			final UserFetcher[] userFetchers,
			final UserStore userStore) {
		Validate.noNullElements(passwordAuthenticators);
		Validate.noNullElements(clientRequestAuthenticators);
		Validate.noNullElements(userFetchers);
		Validate.notNull(userStore);
		this.passwordAuthenticators = passwordAuthenticators;
		this.clientRequestAuthenticators = clientRequestAuthenticators;
		this.userFetchers = userFetchers;
		this.userStore = userStore;
	}

	public AuthenticatedUser authenticate(final Login login, final String password) {
		for (PasswordAuthenticator pa : passwordAuthenticators) {
			if (pa.checkPassword(login, password)) {
				return fetchUser(login, new FetchCallback() {

					@Override
					public void foundUser(final AuthenticatedUser authUser) {
						userStore.setUser(authUser);
					}
				});
			}
		}
		return ANONYMOUS_USER;
	}

	public void authenticate(final Login login, final PasswordCallback passwordCallback) {
		for (PasswordAuthenticator pa : passwordAuthenticators) {
			final String pass = pa.fetchUnencryptedPassword(login);
			if (pass != null) {
				fetchUser(login, new FetchCallback() {

					@Override
					public void foundUser(final AuthenticatedUser authUser) {
						userStore.setUser(authUser);
						passwordCallback.setPassword(pass);
					}
				});
			}
		}
	}

	public ClientAuthenticatorResponse authenticate(final ClientRequest request) {
		for (ClientRequestAuthenticator cra : clientRequestAuthenticators) {
			ClientRequestAuthenticator.Response response = cra.authenticate(request);
			if (response != null) {
				final AuthenticatedUser authUser = fetchUser(response.getLogin(), new FetchCallback() {
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

	public AuthenticatedUser impersonate(final Login login) {
		throw new UnsupportedOperationException("Not implemented");
	}



	private AuthenticatedUser fetchUser(final Login login, final FetchCallback callback) {
		AuthenticatedUser authUser = ANONYMOUS_USER;
		if (login != null) {
			for (UserFetcher uf : userFetchers) {
				final CMUser user = uf.fetchUser(login);
				if (user != null) {
					authUser = AuthenticatedUser.newInstance(user);
					callback.foundUser(authUser);
					break;
				}
			}
		}
		return authUser;
	}
}
