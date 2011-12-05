package unit;

import org.cmdbuild.auth.acl.DefaultPrivileges;
import org.cmdbuild.auth.acl.DefaultPrivileges.SimplePrivilege;
import static org.junit.Assert.*;

import org.junit.Test;

public class DefaultPrivilegesTest {

	@Test
	public void simplePrivilegesAreIndependent() {
		assertFalse(new SimplePrivilege().implies(new SimplePrivilege()));
	}

	@Test
	public void writeImpliesReadPrivilege() {
		assertTrue(DefaultPrivileges.WRITE.implies(DefaultPrivileges.READ));
		assertTrue(DefaultPrivileges.WRITE.implies(DefaultPrivileges.WRITE));
		assertFalse(DefaultPrivileges.READ.implies(DefaultPrivileges.WRITE));
	}

	@Test
	public void godImpliesEveryPrivilege() {
		assertTrue(DefaultPrivileges.GOD.implies(DefaultPrivileges.READ));
		assertTrue(DefaultPrivileges.GOD.implies(DefaultPrivileges.WRITE));
		assertTrue(DefaultPrivileges.GOD.implies(new SimplePrivilege()));
	}
}
