package org.cmdbuild.auth.acl;

import java.util.List;


public interface PrivilegeSet {

	class PrivilegePair {
		public final String name;
		public final CMPrivilege privilege;

		public PrivilegePair(final CMPrivilege privilege) {
			this.name = AbstractSecurityManager.GLOBAL_PRIVILEGE.getPrivilegeId();
			this.privilege = privilege;
		}

		public PrivilegePair(final CMPrivilegedObject object, final CMPrivilege privilege) {
			this.name = object.getPrivilegeId();
			this.privilege = privilege;
		}

		public PrivilegePair(final String name, final CMPrivilege privilege) {
			this.name = name;
			this.privilege = privilege;
		}
	}

	List<PrivilegePair> getAllPrivileges();
}
