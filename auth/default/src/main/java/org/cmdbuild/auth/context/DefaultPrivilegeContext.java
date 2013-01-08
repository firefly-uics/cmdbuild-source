package org.cmdbuild.auth.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.CMPrivilegedObject;
import org.cmdbuild.auth.acl.DefaultPrivileges;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.acl.PrivilegePair;
import org.cmdbuild.common.Builder;

public class DefaultPrivilegeContext implements PrivilegeContext {

	public static class DefaultPrivilegeContextBuilder implements Builder<DefaultPrivilegeContext> {

		private final Map<String, List<CMPrivilege>> objectPrivileges;

		private DefaultPrivilegeContextBuilder() {
			objectPrivileges = new HashMap<String, List<CMPrivilege>>();
		}

		public void withPrivilege(final CMPrivilege privilege, final CMPrivilegedObject object) {
			Validate.notNull(object);
			Validate.notNull(privilege);
			addPrivilege(privilege, object.getPrivilegeId());
		}

		public void withPrivilege(final CMPrivilege privilege) {
			Validate.notNull(privilege);
			addPrivilege(privilege, DefaultPrivileges.GLOBAL_PRIVILEGE_ID);
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

		private List<CMPrivilege> getOrCreatePrivilegeList(final String privilegeId) {
			final List<CMPrivilege> grantedPrivileges;
			if (objectPrivileges.containsKey(privilegeId)) {
				grantedPrivileges = objectPrivileges.get(privilegeId);
			} else {
				grantedPrivileges = new ArrayList<CMPrivilege>(1);
				objectPrivileges.put(privilegeId, grantedPrivileges);
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
		public DefaultPrivilegeContext build() {
			return new DefaultPrivilegeContext(this);
		}

		public Map<String, List<CMPrivilege>> getObjectPrivileges() {
			return objectPrivileges;
		}

	}

	public static DefaultPrivilegeContextBuilder newBuilderInstance() {
		return new DefaultPrivilegeContextBuilder();
	}

	private final Map<String, List<CMPrivilege>> objectPrivileges;

	private DefaultPrivilegeContext(final DefaultPrivilegeContextBuilder builder) {
		this.objectPrivileges = builder.getObjectPrivileges();
	}

	@Override
	public boolean hasAdministratorPrivileges() {
		return hasPrivilege(DefaultPrivileges.ADMINISTRATOR);
	}

	@Override
	public boolean hasDatabaseDesignerPrivileges() {
		return hasPrivilege(DefaultPrivileges.DATABASE_DESIGNER);
	}

	@Override
	public boolean hasPrivilege(final CMPrivilege privilege) {
		return hasPrivilege(privilege, DefaultPrivileges.GLOBAL_PRIVILEGE_ID);
	}

	@Override
	public boolean hasReadAccess(final CMPrivilegedObject privilegedObject) {
		return hasPrivilege(DefaultPrivileges.READ, privilegedObject);
	}

	@Override
	public boolean hasWriteAccess(final CMPrivilegedObject privilegedObject) {
		return hasPrivilege(DefaultPrivileges.WRITE, privilegedObject);
	}

	@Override
	public boolean hasPrivilege(final CMPrivilege requested, final CMPrivilegedObject privilegedObject) {
		return hasPrivilege(requested, DefaultPrivileges.GLOBAL_PRIVILEGE_ID)
				|| hasPrivilege(requested, privilegedObject.getPrivilegeId());
	}

	private final boolean hasPrivilege(final CMPrivilege requested, final String privilegeId) {
		final List<CMPrivilege> grantedPrivileges = objectPrivileges.get(privilegeId);
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

}
