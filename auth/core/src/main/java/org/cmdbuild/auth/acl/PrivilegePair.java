package org.cmdbuild.auth.acl;

public class PrivilegePair {

	public static final SerializablePrivelege GLOBAL_PRIVILEGE = new SerializablePrivelege() {

		@Override
		public String getPrivilegeId() {
			return DefaultPrivileges.GLOBAL_PRIVILEGE_ID;
		}

		@Override
		public String getName() {
			return "";
		}

		@Override
		public String getDescription() {
			return "";
		}

		@Override
		public Long getId() {
			return new Long(0);
		}
	};

	public final String name;
	public SerializablePrivelege privilegedObject;
	public final CMPrivilege privilege;

	public PrivilegePair(final CMPrivilege privilege) {
		this.name = GLOBAL_PRIVILEGE.getPrivilegeId();
		this.privilegedObject = GLOBAL_PRIVILEGE;
		this.privilege = privilege;
	}

	public PrivilegePair(final SerializablePrivelege privilegedObject, final CMPrivilege privilege) {
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
