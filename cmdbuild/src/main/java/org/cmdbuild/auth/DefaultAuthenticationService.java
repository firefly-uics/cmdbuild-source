package org.cmdbuild.auth;

import static org.cmdbuild.auth.user.AuthenticatedUserImpl.ANONYMOUS_USER;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.Alias.as;
import static org.cmdbuild.dao.query.clause.join.Over.over;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.cmdbuild.auth.ClientRequestAuthenticator.ClientRequest;
import org.cmdbuild.auth.Login.LoginType;
import org.cmdbuild.auth.PasswordAuthenticator.PasswordChanger;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.password.NaivePasswordHandler;
import org.cmdbuild.auth.password.PasswordHandler;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.AuthenticatedUserImpl;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entry.DBRelation;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import static org.cmdbuild.dao.query.clause.AnyAttribute.*;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause.Operator;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.auth.UserDTO;
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
	private CMDataView view;

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
		view = TemporaryObjectsBeforeSpringDI.getSystemView();
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

	@Override
	public CMUser fetchUserByUsername(final String username) {
		final Login login = Login.newInstance(username);
		return fetchUser(login);
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
	public CMUser createUser(UserDTO userDTO) {
		PasswordHandler passwordHandler = new NaivePasswordHandler();
		CMCardDefinition newUserCard = view.newCard(userClass());
		newUserCard.set("Description", userDTO.getDescription());
		newUserCard.set("Username", userDTO.getUsername());
		newUserCard.set("Password", passwordHandler.encrypt(userDTO.getPassword()));
		newUserCard.set("Email", userDTO.getEmail());
		newUserCard.set("Active", userDTO.isActive());
		CMCard createdUserCard = newUserCard.save();
		return fetchUserById(createdUserCard.getId());
	}

	@Override
	public CMUser updateUser(UserDTO userDTO) {
		// the username cannot be updated
		CMCard userCard = fetchUserCardWithUserId(userDTO.getUserId());
		CMCardDefinition cardToBeUpdated = view.modifyCard(userCard);
		cardToBeUpdated.set("Active", userDTO.isActive()) //
				.set("Description", userDTO.getDescription()) //
				.set("Email", userDTO.getEmail()) //
				.save();
		if (userDTO.getDefaultGroupId() != null || userDTO.getDefaultGroupId() != 0) {
			DBRelation defaultGroupRelation = fetchRelationForDefaultGroup(userDTO.getUserId());
			if (defaultGroupRelation != null) {
				defaultGroupRelation.set("DefaultGroup", false).save();
			}
			setDefaultGroupToUser(userDTO.getUserId(), userDTO.getDefaultGroupId());
		}
		return fetchUserById(userDTO.getUserId());
	}

	private CMCard fetchUserCardWithUserId(final Long userId) throws NoSuchElementException {
		final Alias userClassAlias = Alias.canonicalAlias(userClass());
		final CMQueryRow userRow = view.select(attribute(userClassAlias, "Username"), //
				attribute(userClassAlias, "Description"), //
				attribute(userClassAlias, "Password")) //
				.from(userClass(), as(userClassAlias)) //
				.where(attribute(userClassAlias, "Id"), Operator.EQUALS, userId).run().getOnlyRow();
		final CMCard userCard = userRow.getCard(userClassAlias);
		return userCard;
	}

	/**
	 * @return null if the user does not have a default group
	 */
	private DBRelation fetchRelationForDefaultGroup(final Long userId) {
		List<DBRelation> relations = fetchRelationsForUserWithId(userId);
		for (final DBRelation relation : relations) {
			final Object isDefaultGroup = relation.get("DefaultGroup");
			if (isDefaultGroup != null)
				if ((Boolean) isDefaultGroup) {
					return (DBRelation) relation;
				}
		}
		return null;
	}

	private List<DBRelation> fetchRelationsForUserWithId(Long userId) {
		final CMQueryResult result = view
				.select(attribute(userClass(), "Username"), anyAttribute(userGroupDomain()),
						attribute(roleClass(), roleClass().getCodeAttributeName())) //
				.from(userClass()) //
				.join(roleClass(), over(userGroupDomain())) //
				.where(attribute(userClass(), "Id"), Operator.EQUALS, userId) //
				.run();
		List<DBRelation> relations = Lists.newArrayList();
		for (final CMQueryRow row : result) {
			final DBRelation relation = row.getRelation(userGroupDomain()).getRelation();
			relations.add(relation);
		}
		return relations;
	}

	private void setDefaultGroupToUser(Long userId, Long defaultGroupId) {
		List<DBRelation> relations = fetchRelationsForUserWithId(userId);
		for (final DBRelation relation : relations) {
			if (relation.getCard2().getId().equals(defaultGroupId)) {
				relation.set("DefaultGroup", true).save();
			}
		}
	}

	@Override
	public Iterable<CMGroup> fetchAllGroups() {
		return groupFetcher.fetchAllGroups();
	}

	@Override
	public List<CMUser> fetchAllUsers() {
		for (final UserFetcher uf : userFetchers) {
			return uf.fetchAllUsers();
		}
		return Lists.newArrayList();
	}

	@Override
	public CMGroup fetchGroupWithId(Long groupId) {
		return groupFetcher.fetchGroupWithId(groupId);
	}

	@Override
	public CMGroup changeGroupStatusTo(Long groupId, boolean isActive) {
		return groupFetcher.changeGroupStatusTo(groupId, isActive);
	}

	private CMClass userClass() {
		return view.findClassByName("User");
	}

	private CMClass roleClass() {
		return view.findClassByName("Role");
	}

	private CMDomain userGroupDomain() {
		return view.findDomainByName("UserRole");
	}

}
