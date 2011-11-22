package unit;

import java.util.ArrayList;
import static org.junit.Assert.*;
import static org.hamcrest.core.Is.is;

import java.util.List;
import org.cmdbuild.auth.acl.AbstractSecurityManager;
import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.CMPrivilegedObject;
import org.cmdbuild.auth.acl.DefaultPrivileges;
import org.cmdbuild.auth.acl.DefaultPrivileges.SimplePrivilege;
import org.cmdbuild.auth.acl.PrivilegeSet.PrivilegePair;
import org.cmdbuild.auth.acl.SimpleSecurityManager;
import org.cmdbuild.auth.acl.SimpleSecurityManager.SimpleSecurityManagerBuilder;
import org.junit.Test;

public class SimpleSecurityManagerTest {

	private static class SimplePrivilegedObject implements CMPrivilegedObject {

		private final String privilegeId;

		private SimplePrivilegedObject(final String privilegeId) {
			this.privilegeId = privilegeId;
		}

		@Override
		public String getPrivilegeId() {
			return privilegeId;
		}
	}

	private static final CMPrivilegedObject DUMMY_PRIV_OBJECT = new SimplePrivilegedObject("dummy");

	private static final CMPrivilege IMPLIED = new SimplePrivilege();
	private static final CMPrivilege IMPLYING = new SimplePrivilege() {

		@Override
		public boolean implies(CMPrivilege privilege) {
			return super.implies(privilege) || privilege == IMPLIED;
		}
	};

	private final SimpleSecurityManagerBuilder builder = SimpleSecurityManager.newInstanceBuilder();

	/*
	 * Builder tests
	 */

	@Test(expected = IllegalArgumentException.class)
	public void nullGlobalPrivilegeCannotBeAdded() {
		builder.withPrivilege(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullPrivilegedObjectCannotBeAdded() {
		builder.withPrivilege(new SimplePrivilege(), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullPrivilegeCannotBeAdded() {
		builder.withPrivilege(null, DUMMY_PRIV_OBJECT);
	}

	@Test
	public void globalPrivilegesAreRegisteredOnTheGlobalObject() {
		builder.withPrivilege(IMPLIED);
		SimpleSecurityManager ssm = builder.build();

		List<PrivilegePair> privileges = ssm.getAllPrivileges();
		assertThat(privileges.size(), is(1));
		assertThat(privileges.get(0).name, is(AbstractSecurityManager.GLOBAL_PRIVILEGE.getPrivilegeId()));
	}

	@Test
	public void onjectPrivilegesAreRegisteredOnThatObject() {
		builder.withPrivilege(IMPLIED, DUMMY_PRIV_OBJECT);
		SimpleSecurityManager ssm = builder.build();

		List<PrivilegePair> privileges = ssm.getAllPrivileges();
		assertThat(privileges.size(), is(1));
		assertThat(privileges.get(0).name, is(DUMMY_PRIV_OBJECT.getPrivilegeId()));
	}

	@Test
	public void samePrivilegeIsNotRegisteredTwice() {
		builder.withPrivilege(IMPLIED);
		builder.withPrivilege(IMPLIED);
		SimpleSecurityManager ssm = builder.build();

		assertThat(ssm.getAllPrivileges().size(), is(1));
	}

	@Test
	public void differentPrivilegesAreBothRegistered() {
		builder.withPrivilege(new SimplePrivilege());
		builder.withPrivilege(new SimplePrivilege());
		SimpleSecurityManager ssm = builder.build();

		assertThat(ssm.getAllPrivileges().size(), is(2));
	}

	@Test
	public void privilegesAreUntouchedIfAlreadyImplied() {
		builder.withPrivilege(IMPLYING);
		builder.withPrivilege(IMPLIED);
		SimpleSecurityManager ssm = builder.build();

		List<PrivilegePair> privileges = ssm.getAllPrivileges();
		assertThat(privileges.size(), is(1));
		assertThat(privileges.get(0).privilege, is(IMPLYING));
	}

	@Test
	public void listOfPrivilegesAreMergedAsSinglePrivileges() {
		final CMPrivilegedObject a = new SimplePrivilegedObject("a");
		final CMPrivilegedObject b = new SimplePrivilegedObject("b");
		final CMPrivilegedObject c = new SimplePrivilegedObject("c");
		final CMPrivilegedObject d = new SimplePrivilegedObject("d");
		final CMPrivilegedObject e = new SimplePrivilegedObject("e");

		builder.withPrivileges(new ArrayList<PrivilegePair>() {{
			add(new PrivilegePair(a.getPrivilegeId(), IMPLIED));
			add(new PrivilegePair(b.getPrivilegeId(), IMPLYING));
			add(new PrivilegePair(c.getPrivilegeId(), new SimplePrivilege()));
			add(new PrivilegePair(d.getPrivilegeId(), new SimplePrivilege()));
		}});
		builder.withPrivileges(new ArrayList<PrivilegePair>() {{
			add(new PrivilegePair(a.getPrivilegeId(), IMPLYING));
			add(new PrivilegePair(b.getPrivilegeId(), IMPLIED));
			add(new PrivilegePair(c.getPrivilegeId(), new SimplePrivilege()));
			add(new PrivilegePair(e.getPrivilegeId(), new SimplePrivilege()));
		}});
		SimpleSecurityManager ssm = builder.build();

		assertThat(ssm.getPrivilegesFor(a).size(), is(1));
		assertThat(ssm.getPrivilegesFor(b).size(), is(1));
		assertThat(ssm.getPrivilegesFor(c).size(), is(2));
		assertThat(ssm.getPrivilegesFor(d).size(), is(1));
		assertThat(ssm.getPrivilegesFor(e).size(), is(1));
	}

	/*
	 * SimpleSecurityManager tests
	 */

	@Test
	public void ifEmptyItHasNoPrivileges() {
		SimpleSecurityManager ssm = builder.build();

		assertThat(ssm.getAllPrivileges().size(), is(0));
		assertFalse(ssm.hasAdministratorPrivileges());
		assertFalse(ssm.hasDatabaseDesignerPrivileges());
	}

	@Test
	public void globalPrivilegesAreAppliedToEveryObject() {
		builder.withPrivilege(IMPLIED);
		SimpleSecurityManager ssm = builder.build();

		assertTrue(ssm.hasPrivilege(IMPLIED));
		assertTrue(ssm.hasPrivilege(IMPLIED, DUMMY_PRIV_OBJECT));
	}

	@Test
	public void objectPrivilegesAreNotAppliedGlobally() {
		builder.withPrivilege(IMPLIED, DUMMY_PRIV_OBJECT);
		SimpleSecurityManager ssm = builder.build();

		assertFalse(ssm.hasPrivilege(IMPLIED));
		assertTrue(ssm.hasPrivilege(IMPLIED, DUMMY_PRIV_OBJECT));
	}

	@Test
	public void withGodPrivilegesYouCanDoEverything() {
		builder.withPrivilege(DefaultPrivileges.GOD);
		SimpleSecurityManager ssm = builder.build();

		assertTrue(ssm.hasPrivilege(DefaultPrivileges.ADMINISTRATOR));
		assertTrue(ssm.hasPrivilege(DefaultPrivileges.DATABASE_DESIGNER));
		assertTrue(ssm.hasReadAccess(DUMMY_PRIV_OBJECT));
		assertTrue(ssm.hasWriteAccess(DUMMY_PRIV_OBJECT));
	}

	@Test
	public void readPrivilegeGrantsReadAccess() {
		builder.withPrivilege(DefaultPrivileges.READ, DUMMY_PRIV_OBJECT);
		SimpleSecurityManager ssm = builder.build();

		assertTrue(ssm.hasReadAccess(DUMMY_PRIV_OBJECT));
		assertFalse(ssm.hasWriteAccess(DUMMY_PRIV_OBJECT));
	}

	@Test
	public void writePrivilegeGrantsReadAndWriteAccess() {
		builder.withPrivilege(DefaultPrivileges.WRITE, DUMMY_PRIV_OBJECT);
		SimpleSecurityManager ssm = builder.build();

		assertTrue(ssm.hasReadAccess(DUMMY_PRIV_OBJECT));
		assertTrue(ssm.hasWriteAccess(DUMMY_PRIV_OBJECT));
	}

}
