package org.cmdbuild.services.auth;

import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.auth.acl.CMPrivilegedObject;
import org.cmdbuild.auth.acl.DefaultPrivileges;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entrytype.CMEntryType;

/**
 * Wrapper for the CMAccessControlManager on top of the legacy UserContext
 */
public class OperationUserWrapper implements OperationUser {

	private final static UserContext systemContext = UserContext.systemContext(); 
	final UserContext userCtx;

	public OperationUserWrapper(final UserContext userCtx) {
		this.userCtx = userCtx;
	}

	@Override
	public String getOperationUsername() {
		return userCtx.getRequestedUsername();
	}

	@Override
	public String getPreferredGroupName() {
		return userCtx.getDefaultGroup().getName();
	}

	@Override
	public boolean hasReadAccess(CMPrivilegedObject privilegedObject) {
		if (privilegedObject instanceof CMEntryType) {
			final CMEntryType type = (CMEntryType) privilegedObject;
			if (type instanceof CMClass) {
				return userCtx.privileges().hasReadPrivilege(systemContext.tables().get(type.getName()));
			} else {
				return userCtx.privileges().hasReadPrivilege(systemContext.domains().get(type.getName()));
			}
		}
		return false;
	}

	@Override
	public boolean hasWriteAccess(CMPrivilegedObject privilegedObject) {
		if (privilegedObject instanceof CMEntryType) {
			final CMEntryType type = (CMEntryType) privilegedObject;
			if (type instanceof CMClass) {
				return userCtx.privileges().hasWritePrivilege(systemContext.tables().get(type.getName()));
			} else {
				return userCtx.privileges().hasWritePrivilege(systemContext.domains().get(type.getName()));
			}
		}
		return false;
	}

	@Override
	public boolean hasAdministratorPrivileges() {
		return userCtx.privileges().isAdmin();
	}

	@Override
	public boolean hasDatabaseDesignerPrivileges() {
		return userCtx.privileges().isAdmin();
	}

	@Override
	public boolean hasPrivilege(CMPrivilege privilege) {
		return userCtx.privileges().isAdmin();
	}

	@Override
	public boolean hasPrivilege(CMPrivilege requested, CMPrivilegedObject privilegedObject) {
		if (requested == DefaultPrivileges.READ) {
			return hasReadAccess(privilegedObject);
		}
		if (requested == DefaultPrivileges.WRITE) {
			return hasWriteAccess(privilegedObject);
		}
		return userCtx.privileges().isAdmin();
	}
}
