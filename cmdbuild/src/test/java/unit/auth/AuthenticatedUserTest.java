package unit.auth;

import org.cmdbuild.auth.acl.InspectableSecurityManager;
import org.cmdbuild.auth.acl.CMPrivilege;
import java.util.List;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import org.cmdbuild.auth.acl.CMPrivilegedObject;
import org.cmdbuild.auth.acl.PrivilegeSet.PrivilegePair;
import org.cmdbuild.auth.acl.DefaultPrivileges.SimplePrivilege;
import org.cmdbuild.auth.PasswordAuthenticator.PasswordChanger;
import org.cmdbuild.auth.AuthenticatedUserImpl;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.DefaultPrivileges;
import org.cmdbuild.auth.acl.GroupImpl;
import org.cmdbuild.auth.user.*;

import org.junit.Test;
import static org.cmdbuild.auth.AuthenticatedUserImpl.ANONYMOUS_USER;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;

public class AuthenticatedUserTest {

	private static class AuthenticatedUserDouble extends AuthenticatedUserImpl {

		private AuthenticatedUserDouble(final CMUser user) {
			super(user);
		}

		@Override
		public CMGroup getPreferredGroup() {
			return super.getPreferredGroup();
		}

		public List<PrivilegePair> getCurrentPrivileges() {
			return super.getSecurityManager().getAllPrivileges();
		}
	}

	private final CMUser innerUser = mock(CMUser.class);
	final CMPrivilegedObject po1;
	private final CMGroup g1;
	private final CMGroup g2;

	public AuthenticatedUserTest() {
		po1 = new CMPrivilegedObject() {

			@Override
			public String getPrivilegeId() {
				return "pid";
			}

		};

		g1 = GroupImpl.newInstanceBuilder()
				.withName("g1")
				.withPrivilege(new PrivilegePair(po1, DefaultPrivileges.READ))
				.withPrivilege(new PrivilegePair(new SimplePrivilege()))
				.build();

		g2 = GroupImpl.newInstanceBuilder()
				.withName("g2")
				.withPrivilege(new PrivilegePair(po1, DefaultPrivileges.WRITE))
				.withPrivilege(new PrivilegePair(new SimplePrivilege()))
				.build();
	}

	/*
	 * Factory method
	 */

	@Test
	public void nullUserCreatesAnAnonymousUser() {
		assertThat(AuthenticatedUserImpl.newInstance(null), is(ANONYMOUS_USER));
	}

	@Test
	public void notNullUserCreatesARegularUser() {
		assertThat(AuthenticatedUserImpl.newInstance(innerUser), is(not(ANONYMOUS_USER)));
	}

	/*
	 * Anonymous user class
	 */

	@Test
	public void anonymousUserHasNoGroup() {
		assertTrue(ANONYMOUS_USER.getGroups().isEmpty());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void anonymousUserCannotSetPasswordChanger() {
		ANONYMOUS_USER.setPasswordChanger(null);
	}

	@Test
	public void anonymousUserIsInvalid() {
		assertFalse(ANONYMOUS_USER.isValid());
	}

	/*
	 * CMUser wrap
	 */

	@Test
	public void forwardsCallsToTheInnerUser() {
		when(innerUser.getName()).thenReturn("inner");
		when(innerUser.getDescription()).thenReturn("Inner");
		when(innerUser.getGroups()).thenReturn(Collections.EMPTY_SET);
		when(innerUser.getDefaultGroupName()).thenReturn("group");
		AuthenticatedUserImpl au = AuthenticatedUserImpl.newInstance(innerUser);

		assertThat(au.getName(), is("inner"));
		assertThat(au.getDescription(), is("Inner"));
		assertThat(au.getGroups(), is(Collections.EMPTY_SET));
		assertThat(au.getDefaultGroupName(), is("group"));
	}

	/*
	 * CMSecurityManager wrap
	 */

	@Test
	public void forwardsCallsToTheSecurityManager() {
		final CMPrivilege p = new SimplePrivilege();
		final InspectableSecurityManager securityManager = mock(InspectableSecurityManager.class);
		AuthenticatedUserDouble au = new AuthenticatedUserDouble(innerUser) {

			@Override
			protected InspectableSecurityManager getSecurityManager() {
				return securityManager;
			}
		};

		au.hasReadAccess(po1);
		au.hasWriteAccess(po1);
		au.hasDatabaseDesignerPrivileges();
		au.hasAdministratorPrivileges();
		au.hasPrivilege(p);
		au.hasPrivilege(p, po1);

		verify(securityManager, times(1)).hasReadAccess(po1);
		verify(securityManager, times(1)).hasWriteAccess(po1);
		verify(securityManager, times(1)).hasDatabaseDesignerPrivileges();
		verify(securityManager, times(1)).hasAdministratorPrivileges();
		verify(securityManager, times(1)).hasPrivilege(p, po1);
	}

	/*
	 * Password change
	 */

	@Test
	public void cannotChangePasswordIfPasswordChangerWasNotSet() {
		AuthenticatedUserImpl au = AuthenticatedUserImpl.newInstance(innerUser);
		assertFalse(au.canChangePassword());
		assertFalse(au.changePassword("x", "y"));
	}

	@Test
	public void canChangePasswordIfPasswordChangerWasSet() {
		AuthenticatedUserImpl au = AuthenticatedUserImpl.newInstance(innerUser);
		PasswordChanger passwordChanger = mock(PasswordChanger.class);
		au.setPasswordChanger(passwordChanger);

		when(passwordChanger.changePassword(eq("x"), eq("y"))).thenReturn(Boolean.TRUE);

		assertTrue(au.canChangePassword());
		assertTrue(au.changePassword("x", "y"));

		verify(passwordChanger, only()).changePassword(eq("x"), eq("y"));
	}

	/*
	 * Preferred group
	 */

	@Test
	public void allowsSelectingANullGroup() {
		AuthenticatedUserDouble au = new AuthenticatedUserDouble(innerUser);
		au.selectGroup(null);
		assertThat(au.getPreferredGroup(), is(nullValue()));
	}

	@Test
	public void allowsSelectAGroupThatDoesNotExist() {
		AuthenticatedUserDouble au = new AuthenticatedUserDouble(innerUser);
		au.selectGroup("any");
		assertThat(au.getPreferredGroup(), is(nullValue()));
	}

	@Test(expected=IllegalStateException.class)
	public void throwsIllegalStateForGroupNameIfNoGroupSelected() {
		AuthenticatedUserDouble au = new AuthenticatedUserDouble(innerUser);
		assertThat(au.getPreferredGroup(), is(nullValue()));
		au.getPreferredGroupName();
	}

	@Test
	public void canSelectAnExistingGroup() {
		when(innerUser.getGroups()).thenReturn(groupSet(g1, g2));
		AuthenticatedUserDouble au = new AuthenticatedUserDouble(innerUser);

		au.selectGroup(g1.getName());

		assertThat(au.getPreferredGroup(), is(g1));
		assertThat(au.getPreferredGroupName(), is(g1.getName()));
	}

	@Test
	public void aSingleGroupIsAutomaticallySelected() {
		when(innerUser.getGroups()).thenReturn(groupSet(g1));
		AuthenticatedUserDouble au = new AuthenticatedUserDouble(innerUser);

		assertThat(au.getPreferredGroup(), is(g1));
		assertThat(au.getPreferredGroupName(), is(g1.getName()));
	}

	@Test
	public void theDefaultGroupIsAutomaticallySelected() {
		when(innerUser.getGroups()).thenReturn(groupSet(g1, g2));
		when(innerUser.getDefaultGroupName()).thenReturn(g2.getName());
		AuthenticatedUserDouble au = new AuthenticatedUserDouble(innerUser);

		assertThat(au.getPreferredGroup(), is(g2));
		assertThat(au.getPreferredGroupName(), is(g2.getName()));
	}

	@Test
	public void isValidIfThePreferredGroupIsSelected() {
		when(innerUser.getGroups()).thenReturn(groupSet(g1, g2));
		AuthenticatedUserDouble au = new AuthenticatedUserDouble(innerUser);

		assertThat(au.getPreferredGroup(), is(nullValue()));
		assertFalse(au.isValid());

		au.selectGroup(g1.getName());

		assertThat(au.getPreferredGroup(), is(not(nullValue())));
		assertTrue(au.isValid());
	}

	/*
	 * Privileges
	 */

	@Test
	public void ifNoGroupsThereAreNoPrivilegesDefined() {
		AuthenticatedUserDouble au = new AuthenticatedUserDouble(innerUser);

		assertTrue(au.getCurrentPrivileges().isEmpty());
	}

	@Test
	public void privilegesFromAllGroupsAreMergedByDefault() {
		when(innerUser.getGroups()).thenReturn(groupSet(g1, g2));
		AuthenticatedUserDouble au = new AuthenticatedUserDouble(innerUser);

		assertThat(au.getCurrentPrivileges().size(), is(3));
		assertTrue(au.hasReadAccess(po1));
		assertTrue(au.hasWriteAccess(po1));
	}

	@Test
	public void emptyPrivilegeSetIfTheGroupDoesNotExist() {
		when(innerUser.getGroups()).thenReturn(groupSet(g1, g2));
		AuthenticatedUserDouble au = new AuthenticatedUserDouble(innerUser);

		au.filterPrivileges("any");

		assertThat(au.getCurrentPrivileges().size(), is(0));
	}

	@Test
	public void groupPrivilegeSetIfTheGroupExists() {
		when(innerUser.getGroups()).thenReturn(groupSet(g1, g2));
		AuthenticatedUserDouble au = new AuthenticatedUserDouble(innerUser);

		au.filterPrivileges(g1.getName());

		assertThat(au.getCurrentPrivileges().size(), is(2));
		assertTrue(au.hasReadAccess(po1));
		assertFalse(au.hasWriteAccess(po1));
	}

	/*
	 * Utility methods
	 */

	private Set<CMGroup> groupSet(CMGroup ... groupArray) {
		Set<CMGroup> groups = new HashSet<CMGroup>();
		for (final CMGroup g : groupArray) {
			groups.add(g);
		}
		return groups;
	}
}
