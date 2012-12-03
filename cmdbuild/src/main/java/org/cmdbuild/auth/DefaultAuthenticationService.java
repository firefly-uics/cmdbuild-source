package org.cmdbuild.auth;

import static org.cmdbuild.auth.user.AuthenticatedUserImpl.ANONYMOUS_USER;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.cmdbuild.auth.ClientRequestAuthenticator.ClientRequest;
import org.cmdbuild.auth.Login.LoginType;
import org.cmdbuild.auth.PasswordAuthenticator.PasswordChanger;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.AuthenticatedUserImpl;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.auth.user.OperationUser;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

public class DefaultAuthenticationService implements AuthenticationService {

	public interface Configuration {
		/**
		 * Returns the names of the authenticators that should be activated, or
		 * null if all authenticators should be activated.
		 * 
		 * @return active authenticators or null
		 */
		Set<String> getActiveAuthenticators();

		/**
		 * Return the names of the service users. They can only log in with
		 * password callback and can impersonate other users. Null means that
		 * there is no service user defined.
		 * 
		 * @return a list of service users or null
		 */
		Set<String> getServiceUsers();
	}

	public static class ClientAuthenticatorResponse {

		private final AuthenticatedUser user;
		private final String redirectUrl;

		public static final ClientAuthenticatorResponse EMTPY_RESPONSE = new ClientAuthenticatorResponse(
				ANONYMOUS_USER, null);

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

	private static final UserStore DUMB_STORE = new UserStore() {

		@Override
		public OperationUser getUser() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setUser(final OperationUser user) {
			throw new UnsupportedOperationException();
		}
	};

	private PasswordAuthenticator[] passwordAuthenticators;
	private ClientRequestAuthenticator[] clientRequestAuthenticators;
	private UserFetcher[] userFetchers;
	private GroupFetcher groupFetcher;
	private UserStore userStore;

	private final Set<String> serviceUsers;
	private final Set<String> authenticatorNames;

	public DefaultAuthenticationService() {
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
	public DefaultAuthenticationService(final Configuration conf) {
		Validate.notNull(conf);
		this.serviceUsers = conf.getServiceUsers();
		this.authenticatorNames = conf.getActiveAuthenticators();
		passwordAuthenticators = new PasswordAuthenticator[0];
		clientRequestAuthenticators = new ClientRequestAuthenticator[0];
		userFetchers = new UserFetcher[0];
		userStore = DUMB_STORE;
	}

	@Override
	public void setPasswordAuthenticators(final PasswordAuthenticator... passwordAuthenticators) {
		Validate.noNullElements(passwordAuthenticators);
		this.passwordAuthenticators = passwordAuthenticators;
	}

	@Override
	public void setClientRequestAuthenticators(final ClientRequestAuthenticator... clientRequestAuthenticators) {
		Validate.noNullElements(clientRequestAuthenticators);
		this.clientRequestAuthenticators = clientRequestAuthenticators;
	}

	@Override
	public void setUserFetchers(final UserFetcher... userFetchers) {
		Validate.noNullElements(userFetchers);
		this.userFetchers = userFetchers;
	}
	
	@Override
	public void setGroupFetcher(final GroupFetcher groupFetcher) {
		Validate.notNull(groupFetcher);
		this.groupFetcher = groupFetcher;
	}

	@Override
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

	@Override
	public AuthenticatedUser authenticate(final Login login, final String password) {
		if (isServiceUser(login)) {
			return ANONYMOUS_USER;
		}
		for (final PasswordAuthenticator passwordAuthenticator : passwordAuthenticators) {
			if (isInactive(passwordAuthenticator)) {
				continue;
			}
			final boolean isUserAuthenticated = passwordAuthenticator.checkPassword(login, password);
			if (isUserAuthenticated) {
				return fetchAuthenticatedUser(login, new FetchCallback() {

					@Override
					public void foundUser(final AuthenticatedUser authUser) {
						final PasswordChanger passwordChanger = passwordAuthenticator.getPasswordChanger(login);
						authUser.setPasswordChanger(passwordChanger);
					}
				});
			}
		}
		return ANONYMOUS_USER;
	}

	@Override
	public AuthenticatedUser authenticate(final Login login, final PasswordCallback passwordCallback) {
		for (final PasswordAuthenticator pa : passwordAuthenticators) {
			if (isInactive(pa)) {
				continue;
			}
			final String pass = pa.fetchUnencryptedPassword(login);
			if (pass != null) {
				return fetchAuthenticatedUser(login, new FetchCallback() {

					@Override
					public void foundUser(final AuthenticatedUser authUser) {
						final PasswordChanger passwordChanger = pa.getPasswordChanger(login);
						authUser.setPasswordChanger(passwordChanger);
						passwordCallback.setPassword(pass);
					}
				});
			}
		}
		return ANONYMOUS_USER;
	}

	@Override
	public ClientAuthenticatorResponse authenticate(final ClientRequest request) {
		for (final ClientRequestAuthenticator cra : clientRequestAuthenticators) {
			if (isInactive(cra)) {
				continue;
			}
			final ClientRequestAuthenticator.Response response = cra.authenticate(request);
			if (response != null) {
				final AuthenticatedUser authUser = fetchAuthenticatedUser(response.getLogin(), new FetchCallback() {
					@Override
					public void foundUser(final AuthenticatedUser authUser) {
						// empty for now
					}
				});
				return new ClientAuthenticatorResponse(authUser, response.getRedirectUrl());
			}
		}
		return ClientAuthenticatorResponse.EMTPY_RESPONSE;
	}

	@Override
	public OperationUser impersonate(final Login login) {
		final OperationUser operationUser = userStore.getUser();
		if (operationUser.hasAdministratorPrivileges() || isServiceUser(operationUser.getAuthenticatedUser())) {
			final CMUser user = fetchUser(login);
			operationUser.impersonate(user);
			return operationUser;
		}
		return operationUser;
	}

	@Override
	public OperationUser getOperationUser() {
		return userStore.getUser();
	}

	private AuthenticatedUser fetchAuthenticatedUser(final Login login, final FetchCallback callback) {
		AuthenticatedUser authUser = ANONYMOUS_USER;
		final CMUser user = fetchUser(login);
		if (user != null) {
			authUser = AuthenticatedUserImpl.newInstance(user);
			callback.foundUser(authUser);
		}
		return authUser;
	}

	private CMUser fetchUser(final Login login) {
		CMUser user = null;
		for (final UserFetcher uf : userFetchers) {
			user = uf.fetchUser(login);
			if (user != null) {
				break;
			}
		}
		return user;
	}

	@Override
	public List<CMUser> fetchUsersByGroupId(final Long groupId) {
		List<CMUser> users = Lists.newArrayList();
		for (final UserFetcher userFetcher : userFetchers) {
			users = userFetcher.fetchUsersFromGroupId(groupId);
			if (!users.isEmpty()) {
				break;
			}
		}
		return users;
	}

	@Override
	public CMUser fetchUserById(final Long userId) {
		CMUser user = null;
		for (final UserFetcher userFetcher : userFetchers) {
			user = userFetcher.fetchUserById(userId);
			if (user != null) {
				break;
			}
		}
		return user;
	}

	@Override
	public CMUser fetchUserByUsername(final String username) {
		final Login login = Login.newInstance(username);
		return fetchUser(login);
	}
	
	@Override
	public Iterable<CMGroup> fetchAllGroups() {
		return groupFetcher.fetchAllGroups();
	}

}
