package integration.logic.auth;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.DefaultAuthenticationService;
import org.cmdbuild.auth.LegacyDBAuthenticator;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.AuthenticationLogic.Response;
import org.cmdbuild.logic.auth.LoginDTO;
import org.junit.Before;
import org.junit.Test;

import utils.DBFixture;

public class AuthenticationLogicTest extends DBFixture {

	private static final String ADMIN_USERNAME = "admin";
	private static final String ADMIN_EMAIL = ADMIN_USERNAME + "@tecnoteca.com";
	private static final String ADMIN_PASSWORD = "admin";
	private static final String WRONG_ADMIN_PASSWORD = "wrong_password";
	private static final String SIMPLE_USERNAME = "simple_user";
	private static final String SIMPLE_PASSWORD = "simple_password";

	private AuthenticationLogic authLogic;
	private DBCard adminCard;
	private DBCard userCard;
	private DBCard groupA;
	private DBCard groupB;
	private DBCard groupC;
	private UserStore DUMB_STORE;

	@Before
	public void setUp() {
		final AuthenticationService service = new DefaultAuthenticationService();
		final LegacyDBAuthenticator dbAuthenticator = new LegacyDBAuthenticator(dbView);
		service.setPasswordAuthenticators(dbAuthenticator);
		service.setUserFetchers(dbAuthenticator);
		authLogic = new AuthenticationLogic(service);
		DUMB_STORE = new UserStore() {
			
			OperationUser operationUser;

			@Override
			public OperationUser getUser() {
				return operationUser;
			}

			@Override
			public void setUser(final OperationUser user) {
				this.operationUser = user;
			}
		};

		populateDatabaseWithUsersGroupsAndPrivileges();
	}

	private void populateDatabaseWithUsersGroupsAndPrivileges() {
		adminCard = insertUserWithUsernameAndPassword(ADMIN_USERNAME, ADMIN_PASSWORD);
		userCard = insertUserWithUsernameAndPassword(SIMPLE_USERNAME, SIMPLE_PASSWORD);

		groupA = insertRoleWithCode("group A");
		groupB = insertRoleWithCode("group B");
		groupC = insertRoleWithCode("group C");

		buildNnRelation();
	}

	// TODO: riscrivere i test con i vari casi:
	/**
	 * 1) l'utente fornisce credenziali errate 2) credenziali giuste e
	 * appartiene a un gruppo 3) credenziali giuste e appartiene a + gruppi ma
	 * default group 4) credenziali giuste e appartiene a + gruppi ma non
	 * default group 5) integration test con privilege manager (in un altro file
	 * di test...)
	 */
	@Test
	public void shouldAuthenticateUserWithValidUsernameAndPasswordAndGroupSelected() {
		// given
		final LoginDTO loginDTO = LoginDTO.newInstanceBuilder() //
				.withLoginString(ADMIN_USERNAME) //
				.withPassword(ADMIN_PASSWORD) //
				.withGroupName((String) groupA.getCode()) //
				.withUserStore(DUMB_STORE).build();

		// when
		final Response response = authLogic.login(loginDTO);

		// then
		assertUserIsSuccessfullyAuthenticated(response);
		//TODO: check in the UserStore if the user has been successfully stored
	}

	private void assertUserIsSuccessfullyAuthenticated(final Response response) {
		assertTrue(response.isSuccess());
		assertThat(response.getGroups(), is(nullValue()));
		assertThat(response.getReason(), is(nullValue()));
	}

	@Test
	public void shouldAuthenticateUserWithValidEmailAndPasswordAndGroupSelected() {
		// given
		final LoginDTO loginDTO = LoginDTO.newInstanceBuilder() //
				.withLoginString(ADMIN_EMAIL) //
				.withPassword(ADMIN_PASSWORD) //
				.withGroupName((String) groupA.getCode()) //
				.withUserStore(DUMB_STORE).build();
		// when
		final Response response = authLogic.login(loginDTO);

		// then
		assertUserIsSuccessfullyAuthenticated(response);
		//TODO: check in the UserStore if the user has been successfully stored
	}
	
	@Test
	public void userShouldSelectAGroupIfHeBelongsToMultipleGroupsAndNoDefault() {
		//TODO: implement
	}
	
	@Test
	public void operationUserIsStoredIfHeHasDefaultGroup() {
		//TODO: implement
	}
	
	@Test
	public void operationUserIsStoredIfHeBelongsToOnlyOneGroup() {
		//TODO: implement
	}

	@Test
	public void shouldNotAuthenticateUserWithWrongPassword() {
		// given
		final LoginDTO loginDTO = LoginDTO.newInstanceBuilder() //
				.withLoginString(ADMIN_USERNAME) //
				.withPassword(WRONG_ADMIN_PASSWORD) //
				.withGroupName((String) groupA.getCode()) //
				.withUserStore(DUMB_STORE).build();
		// when
		final Response response = authLogic.login(loginDTO);

		// then
		assertFalse(response.isSuccess());
		assertThat(response.getGroups(), is(nullValue()));
		assertThat(response.getReason(), is(not(nullValue())));
	}

	@Test
	public void shouldNotAuthenticateUserWithWrongUsername() {
		// given
		final LoginDTO loginDTO = LoginDTO.newInstanceBuilder() //
				.withLoginString("wrong_admin_username") //
				.withPassword(ADMIN_PASSWORD) //
				.withGroupName((String) groupA.getCode()) //
				.withUserStore(DUMB_STORE).build();
		// when
		final Response response = authLogic.login(loginDTO);

		// then
		assertFalse(response.isSuccess());
		assertThat(response.getGroups(), is(nullValue()));
		assertThat(response.getReason(), is(not(nullValue())));
	}

	@Test
	public void shouldRetrieveAllGroupsForAUser() {
		// when
		final Iterable<CMGroup> groups = authLogic.getGroupsFromUserId(adminCard.getId());

		// then
		int numberOfGroups = 0;
		for (final CMGroup group : groups) {
			numberOfGroups++;
			final Long groupId = group.getId();
			assertThat(groupIdsForUserId(adminCard.getId()), hasItem(groupId));
		}
		assertEquals(numberOfGroups, groupIdsForUserId(adminCard.getId()).size());
	}

	/**
	 * A user belongs to multiple groups and a group contains more than one user
	 */
	private void buildNnRelation() {
		insertBindingBetweenUserAndRole(adminCard, groupA);
		insertBindingBetweenUserAndRole(adminCard, groupB);
		insertBindingBetweenUserAndRole(userCard, groupB);
	}

	private List<Long> groupIdsForUserId(final Long userId) {
		return userIdToGroupIds.get(userId);
	}

	@Test
	public void shouldRetrieveAllUsersForNonEmptyGroup() {
		// when
		final Iterable<CMUser> users = authLogic.getUsersFromGroupId(groupA.getId());
		int actualNumberOfUsers = 0;
		for (final CMUser user : users) {
			actualNumberOfUsers++;
		}

		// then
		int usersThatActuallyBelongToGroup = 0;
		for (final Long userId : userIdToGroupIds.keySet()) {
			final List<Long> groupIds = userIdToGroupIds.get(userId);
			if (groupIds.contains(groupA.getId())) {
				usersThatActuallyBelongToGroup++;
			}
		}
		assertEquals(usersThatActuallyBelongToGroup, actualNumberOfUsers);
	}

	@Test
	public void shouldRetrieveNoUserForEmptyGroup() {
		// when
		final Iterable<CMUser> users = authLogic.getUsersFromGroupId(groupC.getId());

		// then
		assertEquals(users.iterator().hasNext(), false);
	}

}
