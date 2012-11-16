package integration.logic.auth;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.cmdbuild.auth.AuthenticatedUser;
import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.DefaultAuthenticationService;
import org.cmdbuild.auth.LegacyDBAuthenticator;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.logic.auth.AuthenticationLogic;
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

	@Before
	public void setUp() {
		final AuthenticationService service = new DefaultAuthenticationService();
		final LegacyDBAuthenticator dbAuthenticator = new LegacyDBAuthenticator(dbView);
		service.setUserStore(new UserStore() {

			@Override
			public void setUser(final AuthenticatedUser user) {
				// mock
			}

			@Override
			public AuthenticatedUser getUser() {
				// mock
				return null;
			}
		});
		service.setPasswordAuthenticators(dbAuthenticator);
		service.setUserFetchers(dbAuthenticator);
		authLogic = new AuthenticationLogic(service);

		populateDatabaseWithUsersGroupsAndPrivileges();
	}

	private void populateDatabaseWithUsersGroupsAndPrivileges() {
		adminCard = insertUserWithUsernameAndPassword(ADMIN_USERNAME, ADMIN_PASSWORD);
		userCard = insertUserWithUsernameAndPassword(SIMPLE_USERNAME, SIMPLE_PASSWORD);

		groupA = insertRoleWithCode("group A");
		groupB = insertRoleWithCode("group B");
		groupC = insertRoleWithCode("group C");

	}

	@Test
	public void shouldAuthenticateUserWithValidUsernameAndPassword() {
		// given
		buildNnRelation();

		// when
		final AuthenticatedUser authUser = authLogic.login(ADMIN_USERNAME, ADMIN_PASSWORD);

		// then
		assertUserIsSuccessfullyAuthenticated(authUser);
	}

	@Test
	public void shouldAuthenticateUserWithValidEmailAndPassword() {
		// given
		buildNnRelation();

		// when
		final AuthenticatedUser authUser = authLogic.login(ADMIN_EMAIL, ADMIN_PASSWORD);

		// then
		assertUserIsSuccessfullyAuthenticated(authUser);
	}

	private void assertUserIsSuccessfullyAuthenticated(final AuthenticatedUser authUser) {
		assertNotNull(authUser.getId());
	}

	@Test
	public void shouldNotAuthenticateUserWithWrongPassword() {
		// given
		buildNnRelation();

		// when
		final AuthenticatedUser authUser = authLogic.login(ADMIN_USERNAME, WRONG_ADMIN_PASSWORD);

		// then
		assertTrue(authUser.isAnonymous());
	}

	@Test
	public void shouldNotAuthenticateUserWithWrongUsername() {
		// given
		buildNnRelation();

		// when
		final AuthenticatedUser authUser = authLogic.login("fake_username", WRONG_ADMIN_PASSWORD);

		// then
		assertTrue(authUser.isAnonymous());
	}

	@Test
	public void shouldRetrieveAllGroupsForAUser() {
		// given
		buildNnRelation();

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
		// given
		buildNnRelation();

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
		// given
		buildNnRelation();

		// when
		final Iterable<CMUser> users = authLogic.getUsersFromGroupId(groupC.getId());

		// then
		assertEquals(users.iterator().hasNext(), false);
	}

}
