package integration.logic.auth;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.DBGroupFetcher;
import org.cmdbuild.auth.DefaultAuthenticationService;
import org.cmdbuild.auth.LegacyDBAuthenticator;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.AuthenticationLogic.Response;
import org.cmdbuild.logic.auth.LoginDTO;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import utils.DBFixture;

public class AuthenticationLogicTest extends DBFixture {

	private static final String ADMIN_USERNAME = "admin";
	private static final String ADMIN_EMAIL = ADMIN_USERNAME + "@example.com";
	private static final String ADMIN_PASSWORD = "admin";
	private static final String WRONG_ADMIN_PASSWORD = "wrong_password";
	private static final String SIMPLE_USERNAME = "simple_user";
	private static final String SIMPLE_PASSWORD = "simple_password";
	private static final String USER_DEFAULT_GROUP = "userdef";
	private static final String PASSWORD_DEFAULT_GROUP = "userdef_password";

	private AuthenticationLogic authLogic;
	private DBCard admin;
	private DBCard simpleUser;
	private DBCard userWithDefaultGroup;
	private DBCard groupA;
	private DBCard groupB;
	private DBCard emptyGroup;
	private UserStore IN_MEMORY_STORE;

	@Before
	public void setUp() {
		final AuthenticationService service = new DefaultAuthenticationService();
		final LegacyDBAuthenticator dbAuthenticator = new LegacyDBAuthenticator(dbDataView());
		service.setPasswordAuthenticators(dbAuthenticator);
		service.setUserFetchers(dbAuthenticator);
		service.setGroupFetcher(new DBGroupFetcher(dbDataView()));
		authLogic = new AuthenticationLogic(service);
		IN_MEMORY_STORE = new UserStore() {

			OperationUser operationUser = null;

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
		admin = insertUserWithUsernameAndPassword(ADMIN_USERNAME, ADMIN_PASSWORD);
		simpleUser = insertUserWithUsernameAndPassword(SIMPLE_USERNAME, SIMPLE_PASSWORD);
		userWithDefaultGroup = insertUserWithUsernameAndPassword(USER_DEFAULT_GROUP, PASSWORD_DEFAULT_GROUP);

		groupA = insertRoleWithCode("group A");
		groupB = insertRoleWithCode("group B");
		emptyGroup = insertRoleWithCode("group C");

		createUserRoleBinding();
	}

	/**
	 * A user belongs to multiple groups and a group contains more than one user
	 */
	private void createUserRoleBinding() {
		insertBindingBetweenUserAndRole(admin, groupA);
		insertBindingBetweenUserAndRole(admin, groupB);
		insertBindingBetweenUserAndRole(simpleUser, groupB);
		insertBindingBetweenUserAndRole(userWithDefaultGroup, groupA);
		insertBindingBetweenUserAndRole(userWithDefaultGroup, groupB, true);
	}

	@Test
	public void shouldAuthenticateUserWithValidUsernameAndPasswordAndGroupSelected() {
		// given
		final LoginDTO loginDTO = LoginDTO.newInstanceBuilder() //
				.withLoginString(ADMIN_USERNAME) //
				.withPassword(ADMIN_PASSWORD) //
				.withGroupName((String) groupA.getCode()) //
				.withUserStore(IN_MEMORY_STORE).build();

		// when
		final Response response = authLogic.login(loginDTO);

		// then
		assertUserIsSuccessfullyAuthenticated(response);
		assertOperationUserIsStoredInUserStore();
	}

	private void assertUserIsSuccessfullyAuthenticated(final Response response) {
		assertTrue(response.isSuccess());
		assertThat(response.getGroups(), is(nullValue()));
		assertThat(response.getReason(), is(nullValue()));
	}

	private void assertOperationUserIsStoredInUserStore() {
		assertThat(IN_MEMORY_STORE.getUser(), is(not(nullValue())));
	}

	@Test
	public void shouldAuthenticateUserWithValidEmailAndPasswordAndGroupSelected() {
		// given
		final LoginDTO loginDTO = LoginDTO.newInstanceBuilder() //
				.withLoginString(ADMIN_EMAIL) //
				.withPassword(ADMIN_PASSWORD) //
				.withGroupName((String) groupA.getCode()) //
				.withUserStore(IN_MEMORY_STORE).build();
		// when
		final Response response = authLogic.login(loginDTO);

		// then
		assertUserIsSuccessfullyAuthenticated(response);
		assertOperationUserIsStoredInUserStore();
	}

	@Test
	public void userShouldSelectAGroupIfBelongsToMultipleGroupsAndNoDefault() {
		// given
		final LoginDTO loginDTO = LoginDTO.newInstanceBuilder() //
				.withLoginString(ADMIN_USERNAME) //
				.withPassword(ADMIN_PASSWORD) //
				.withUserStore(IN_MEMORY_STORE) //
				.build();

		// when
		final Response response = authLogic.login(loginDTO);

		// then
		assertFalse(response.isSuccess());
		assertThat(response.getReason(),
				is(equalTo(AuthExceptionType.AUTH_MULTIPLE_GROUPS.createException().toString())));
		assertThat(response.getGroups(), is(not(nullValue())));
		assertOperationUserIsNotStoredInUserStore();
	}

	private void assertOperationUserIsNotStoredInUserStore() {
		assertThat(IN_MEMORY_STORE.getUser(), is(nullValue()));
	}

	@Test
	public void userShouldNotSelectAGroupIfHasDefaultGroup() {
		// given
		final LoginDTO loginDTO = LoginDTO.newInstanceBuilder() //
				.withLoginString(USER_DEFAULT_GROUP) //
				.withPassword(PASSWORD_DEFAULT_GROUP) //
				.withUserStore(IN_MEMORY_STORE) //
				.build();

		// when
		final Response response = authLogic.login(loginDTO);

		// then
		assertTrue(response.isSuccess());
		assertOperationUserIsStoredInUserStore();
	}

	@Test
	public void userShouldNotSelectAGroupIfBelongsToOnlyOneGroup() {
		// given
		final LoginDTO loginDTO = LoginDTO.newInstanceBuilder() //
				.withLoginString(SIMPLE_USERNAME) //
				.withPassword(SIMPLE_PASSWORD) //
				.withUserStore(IN_MEMORY_STORE) //
				.build();

		// when
		final Response response = authLogic.login(loginDTO);

		// then
		assertTrue(response.isSuccess());
		assertOperationUserIsStoredInUserStore();
	}

	@Test
	public void shouldNotAuthenticateUserWithWrongPassword() {
		// given
		final LoginDTO loginDTO = LoginDTO.newInstanceBuilder() //
				.withLoginString(ADMIN_USERNAME) //
				.withPassword(WRONG_ADMIN_PASSWORD) //
				.withGroupName((String) groupA.getCode()) //
				.withUserStore(IN_MEMORY_STORE).build();

		// when
		final Response response = authLogic.login(loginDTO);

		// then
		assertFalse(response.isSuccess());
		assertThat(response.getGroups(), is(nullValue()));
		assertThat(response.getReason(), is(equalTo(AuthExceptionType.AUTH_LOGIN_WRONG.createException().toString())));
		assertOperationUserIsNotStoredInUserStore();
	}

	@Test
	public void shouldNotAuthenticateUserWithWrongUsername() {
		// given
		final LoginDTO loginDTO = LoginDTO.newInstanceBuilder() //
				.withLoginString("wrong_admin_username") //
				.withPassword(ADMIN_PASSWORD) //
				.withGroupName((String) groupA.getCode()) //
				.withUserStore(IN_MEMORY_STORE).build();
		// when
		final Response response = authLogic.login(loginDTO);

		// then
		assertFalse(response.isSuccess());
		assertThat(response.getGroups(), is(nullValue()));
		assertThat(response.getReason(), is(not(nullValue())));
		assertOperationUserIsNotStoredInUserStore();
	}

	@Test
	public void shouldRetrieveAllGroupsForAUser() {
		// when
		final Iterable<CMGroup> groups = authLogic.getGroupsForUserWithId(admin.getId());

		// then
		int numberOfGroups = 0;
		for (final CMGroup group : groups) {
			numberOfGroups++;
			final Long groupId = group.getId();
			assertThat(groupIdsForUserId(admin.getId()), hasItem(groupId));
		}
		assertEquals(numberOfGroups, groupIdsForUserId(admin.getId()).size());
	}

	private List<Long> groupIdsForUserId(final Long userId) {
		return userIdToGroupIds.get(userId);
	}

	@Test
	public void shouldRetrieveAllUsersForNonEmptyGroup() {
		// when
		final Iterable<CMUser> users = authLogic.getUsersForGroupWithId(groupA.getId());
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
		final Iterable<CMUser> users = authLogic.getUsersForGroupWithId(emptyGroup.getId());

		// then
		assertEquals(users.iterator().hasNext(), false);
	}
	
	@Test
	public void shouldRetrieveUserFromId() {
		//given
		Long expectedId = admin.getId();
		
		//when
		CMUser retrievedUser = authLogic.getUserWithId(expectedId);
		
		//then
		assertEquals(expectedId, retrievedUser.getId());
	}
	
	@Test
	public void shouldRetrieveAllGroups() {
		//when
		Iterable<CMGroup> allGroups = authLogic.getAllGroups();
		
		//then
		int numberOfGroups = 0;
		for (CMGroup group : allGroups) {
			numberOfGroups++;
		}
		assertEquals(numberOfGroups, 3);
	}
	
	@Test
	public void shouldRetrieveExistentGroupFromId() {
		//when
		CMGroup retrievedGroup = authLogic.getGroupWithId(groupA.getId());
		
		//then
		assertNotNull(retrievedGroup);
		assertEquals(groupA.getId(), retrievedGroup.getId());
	}
	
	@Test
	public void shouldRetrieveNullGroupIfNonExistentId() {
		//when
		CMGroup retrievedGroup = authLogic.getGroupWithId(-1L);
		
		//then
		assertNotNull(retrievedGroup);
		assertTrue(retrievedGroup instanceof NullGroup);
	}
	
	@Ignore("Until the update of a card is not implemented...")
	@Test
	public void shouldChangeStatusToGroup() {
		//when
		CMGroup updatedGroup = authLogic.changeGroupStatusTo(groupA.getId(), true);
		
		//TODO: complete this test
	}

}
