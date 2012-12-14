package integration.logic.security;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.logic.privileges.SecurityLogic;
import org.cmdbuild.logic.privileges.SecurityLogic.PrivilegeInfo;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import utils.DBFixture;

public class SecurityLogicTest extends DBFixture {

	private static final String ADMIN_USERNAME = "admin";
	private static final String ADMIN_PASSWORD = "admin";
	private static final String SIMPLE_USERNAME = "simple_user";
	private static final String SIMPLE_PASSWORD = "simple_password";
	private static final String USER_DEFAULT_GROUP = "userdef";
	private static final String PASSWORD_DEFAULT_GROUP = "userdef_password";

	private DBCard admin;
	private DBCard simpleUser;
	private DBCard userWithDefaultGroup;
	private DBCard groupA;
	private DBCard groupB;
	private SecurityLogic securityLogic;

	@Before
	public void setUp() {
		securityLogic = new SecurityLogic(dbDataView());
		populateDatabaseWithUsersGroupsAndPrivileges();
	}

	private void populateDatabaseWithUsersGroupsAndPrivileges() {
		admin = insertUserWithUsernameAndPassword(ADMIN_USERNAME, ADMIN_PASSWORD);
		simpleUser = insertUserWithUsernameAndPassword(SIMPLE_USERNAME, SIMPLE_PASSWORD);
		userWithDefaultGroup = insertUserWithUsernameAndPassword(USER_DEFAULT_GROUP, PASSWORD_DEFAULT_GROUP);

		groupA = insertRoleWithCode("group A");
		groupB = insertRoleWithCode("group B");

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
	public void shouldRetrieveAllPrivilegesForGroup() {
		// given
		final DBClass createdClass = dbDriver().createClass(newClass(uniqueUUID(), null));
		final DBCard privilegeCard = insertPrivilege(groupA.getId(), createdClass, "w");

		// when
		final List<PrivilegeInfo> privileges = securityLogic.getPrivilegesForGroup(groupA.getId());

		// then
		assertEquals(privileges.size(), 1);
		final PrivilegeInfo privilege = privileges.get(0);
		assertThat(privilege.getPrivilegeObjectId(), is(equalTo(createdClass.getId())));
		assertThat(privilege.getGroupId(), is(equalTo(groupA.getId())));
		assertThat(privilege.mode, is(equalTo("w")));
	}

	@Test
	public void shouldCreatePrivilegeForExistingClass() {
		// given
		final DBClass createdClass = dbDriver().createClass(newClass(uniqueUUID(), null));
		final int numberOfExistentPrivileges = securityLogic.getPrivilegesForGroup(groupA.getId()).size();

		// when
		final PrivilegeInfo privilegeInfo = new PrivilegeInfo(groupA.getId(), createdClass, "r");
		securityLogic.savePrivilege(privilegeInfo);

		// then
		final List<PrivilegeInfo> groupPrivileges = securityLogic.getPrivilegesForGroup(groupA.getId());
		assertEquals(groupPrivileges.size(), numberOfExistentPrivileges + 1);
		assertThat(groupPrivileges, hasItem(privilegeInfo));
	}

	@Ignore("Because the update card method is not yet implemented")
	@Test
	public void shoulUpdateExistentPrivilege() {
		// given
		final DBClass createdClass = dbDriver().createClass(newClass(uniqueUUID(), null));
		insertPrivilege(groupA.getId(), createdClass, "-");
		final int numberOfExistentPrivileges = securityLogic.getPrivilegesForGroup(groupA.getId()).size();

		// when
		final PrivilegeInfo privilegeInfo = new PrivilegeInfo(groupA.getId(), createdClass, "r");
		securityLogic.savePrivilege(privilegeInfo);

		// then
		final int numberOfActualPrivileges = securityLogic.getPrivilegesForGroup(groupA.getId()).size();
		assertEquals(numberOfExistentPrivileges, numberOfActualPrivileges);
	}

}
