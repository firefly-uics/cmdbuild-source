package org.cmdbuild.auth.acl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.Validate;
import org.cmdbuild.common.Builder;

public class SimpleSecurityManager extends AbstractSecurityManager implements PrivilegeSet {

	public static class SimpleSecurityManagerBuilder implements Builder<SimpleSecurityManager> {

		private final Map<String, List<CMPrivilege>> objectPrivileges;

		private SimpleSecurityManagerBuilder() {
			objectPrivileges = new HashMap<String, List<CMPrivilege>>();
		}

		public void withPrivilege(final CMPrivilege privilege, final CMPrivilegedObject object) {
			Validate.notNull(object);
			Validate.notNull(privilege);
			addPrivilege(privilege, object.getPrivilegeId());
		}

		public void withPrivilege(final CMPrivilege privilege) {
			Validate.notNull(privilege);
			addPrivilege(privilege, GLOBAL_PRIVILEGE_ID);
		}

		public void withPrivileges(final Iterable<PrivilegePair> privileges) {
			Validate.notNull(privileges);
			for (final PrivilegePair pair : privileges) {
				addPrivilege(pair.privilege, pair.name);
			}
		}

		private void addPrivilege(final CMPrivilege privilege, final String privilegeId) {
			final List<CMPrivilege> grantedPrivileges = getOrCreatePrivilegeList(privilegeId);
			mergePrivilege(privilege, grantedPrivileges);
		}

		private List<CMPrivilege> getOrCreatePrivilegeList(final String privId) {
			final List<CMPrivilege> grantedPrivileges;
			if (objectPrivileges.containsKey(privId)) {
				grantedPrivileges = objectPrivileges.get(privId);
			} else {
				grantedPrivileges = new ArrayList<CMPrivilege>(1);
				objectPrivileges.put(privId, grantedPrivileges);
			}
			return grantedPrivileges;
		}
		
		private void mergePrivilege(final CMPrivilege newPrivilege, final List<CMPrivilege> grantedPrivileges) {
						final Iterator<CMPrivilege> iter = grantedPrivileges.iterator();
			while (iter.hasNext()) {
				final CMPrivilege oldPrivilege = iter.next();
				if (oldPrivilege.implies(newPrivilege)) {
					// New pivilege is implied by exising privilege
					return;
				}
				if (newPrivilege.implies(oldPrivilege)) {
					iter.remove();
				}
			}
			grantedPrivileges.add(newPrivilege);
		}

		@Override
		public SimpleSecurityManager build() {
			return new SimpleSecurityManager(this);
		}
	}

	private final Map<String, List<CMPrivilege>> objectPrivileges;

	public static SimpleSecurityManagerBuilder newInstanceBuilder() {
		return new SimpleSecurityManagerBuilder();
	}

	private SimpleSecurityManager(final SimpleSecurityManagerBuilder builder) {
		this.objectPrivileges = builder.objectPrivileges;
	}

	@Override
	public List<PrivilegePair> getAllPrivileges() {
		final List<PrivilegePair> allPrivileges = new ArrayList<PrivilegePair>();
		for (final Map.Entry<String, List<CMPrivilege>> entry : objectPrivileges.entrySet()) {
			for (final CMPrivilege priv : entry.getValue()) {
				final PrivilegePair privPair = new PrivilegePair(entry.getKey(), priv);
				allPrivileges.add(privPair);
			}
		}
		return allPrivileges;
	}

	@Override
	protected final boolean hasPrivilege(final CMPrivilege requested, final String privId) {
		final List<CMPrivilege> grantedPrivileges = objectPrivileges.get(privId);
		if (grantedPrivileges != null) {
			for (final CMPrivilege granted : grantedPrivileges) {
				if (granted.implies(requested)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns the privileges for an object. Used by tests.
	 */
	public List<CMPrivilege> getPrivilegesFor(final CMPrivilegedObject object) {
		return objectPrivileges.get(object.getPrivilegeId());
	}
}
