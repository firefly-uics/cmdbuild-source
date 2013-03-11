package org.cmdbuild.auth.acl;

public class PrivilegePair {

	public static final CMPrivilegedObject GLOBAL_PRIVILEGE = new CMPrivilegedObject() {

		@Override
		public String getPrivilegeId() {
			return DefaultPrivileges.GLOBAL_PRIVILEGE_ID;
		}
	};

	public final String name;
	public CMPrivilegedObject privilegedObject;
	public final CMPrivilege privilege;

	public PrivilegePair(final CMPrivilege privilege) {
		this.name = GLOBAL_PRIVILEGE.getPrivilegeId();
		this.privilegedObject = GLOBAL_PRIVILEGE;
		this.privilege = privilege;
	}

	public PrivilegePair(final CMPrivilegedObject privilegedObject, final CMPrivilege privilege) {
		this.name = privilegedObject.getPrivilegeId();
		this.privilegedObject = privilegedObject;
		this.privilege = privilege;
	}

	/**
	 * @deprecated Must be used only by tests
	 */
	@Deprecated
	public PrivilegePair(final String name, final CMPrivilege privilege) {
		this.name = name;
		this.privilege = privilege;
	}
}
