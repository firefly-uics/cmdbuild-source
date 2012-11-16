package org.cmdbuild.auth.acl;

public abstract class AbstractSecurityManager implements CMSecurityManager {

	protected static final String GLOBAL_PRIVILEGE_ID = null;

	public static final CMPrivilegedObject GLOBAL_PRIVILEGE = new CMPrivilegedObject() {

		@Override
		public String getPrivilegeId() {
			return GLOBAL_PRIVILEGE_ID;
		}
	};

	@Override
	public final boolean hasAdministratorPrivileges() {
		return hasPrivilege(DefaultPrivileges.ADMINISTRATOR);
	}

	@Override
	public final boolean hasDatabaseDesignerPrivileges() {
		return hasPrivilege(DefaultPrivileges.DATABASE_DESIGNER);
	}

	@Override
	public final boolean hasPrivilege(final CMPrivilege privilege) {
		return hasPrivilege(privilege, GLOBAL_PRIVILEGE_ID);
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
		return hasPrivilege(requested, GLOBAL_PRIVILEGE_ID)
				|| hasPrivilege(requested, privilegedObject.getPrivilegeId());
	}

	protected abstract boolean hasPrivilege(final CMPrivilege requested, final String privilegeId);
}
