package unit.auth;

import static java.util.Collections.unmodifiableSet;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.cmdbuild.services.auth.UserInfoBuilder.aUserGroup;
import static org.cmdbuild.services.auth.UserInfoBuilder.aUserGroups;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.cmdbuild.services.auth.UserGroup;
import org.cmdbuild.services.auth.UserInfo;
import org.cmdbuild.services.auth.UserInfoBuilder;
import org.cmdbuild.services.auth.UserType;
import org.junit.Before;
import org.junit.Test;

@Deprecated
public class UserInfoBuilderTest {

	private static final String BLANK = " \t";

	private UserInfoBuilder valid;

	@Before
	public void checkValidOnes() throws Exception {
		valid = new UserInfoBuilder() //
				.setUsername("username") //
				.setUserType(UserType.APPLICATION);

		// checks a valid one specifying a single group
		valid.addUserGroup(aUserGroup("group", "group")).build();

		// checks a valid one specifying all groups
		valid.setUserGroups(aUserGroups(aUserGroup("group", "groups"))).build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullUsernameThrowsException() throws Exception {
		valid.setUsername(null).build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void emptyUsernameThrowsException() throws Exception {
		valid.setUsername(EMPTY).build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void blankUsernameThrowsException() throws Exception {
		valid.setUsername(BLANK).build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullUserTypeThrowsException() throws Exception {
		valid.setUserType(null).build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullUserGroupsThrowsException() throws Exception {
		valid.setUserGroups(null).build();
	}
	
	@Test
	public void groupCanBeAddedAfterThatAnUnmodifiableCollectionCanBeSetted() throws Exception {
		final Set<UserGroup> unmodifiable = unmodifiableSet(aUserGroups(aUserGroup("group", "group")));
		final UserInfo userInfo = valid //
				.setUserGroups(unmodifiable) //
				.addUserGroup(aUserGroup("anotherGroup", "another group")) //
				.build();
		assertThat(userInfo.getGroups().size(), equalTo(2));
	}

}
