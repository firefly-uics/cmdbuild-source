package org.cmdbuild.auth.acl;

public class PrivilegePair {

	public static final CMPrivilegedObject GLOBAL_PRIVILEGE = new CMPrivilegedObject() {

		@Override
		public String getPrivilegeId() {
			return DefaultPrivileges.GLOBAL_PRIVILEGE_ID;
		}
	};

	public final String name;
	public final CMPrivilege privilege;

	public PrivilegePair(final CMPrivilege privilege) {
		this.name = GLOBAL_PRIVILEGE.getPrivilegeId();
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
