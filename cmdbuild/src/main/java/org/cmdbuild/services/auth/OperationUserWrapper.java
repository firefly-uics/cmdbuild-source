package org.cmdbuild.services.auth;

import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.auth.acl.CMPrivilegedObject;
import org.cmdbuild.auth.acl.DefaultPrivileges;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entrytype.CMEntryType;

public class OperationUserWrapper implements OperationUser {

	private final static UserContext systemContext = UserContext.systemContext(); 
	final PrivilegeManager pm;
	final String username;

	public OperationUserWrapper(final UserContext userCtx) {
		this.pm = userCtx.privileges();
		this.username = userCtx.getRequestedUsername();
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean hasReadAccess(CMPrivilegedObject privilegedObject) {
		if (privilegedObject instanceof CMEntryType) {
			final CMEntryType type = (CMEntryType) privilegedObject;
			if (type instanceof CMClass) {
				return pm.hasReadPrivilege(systemContext.tables().get(type.getName()));
			} else {
				return pm.hasReadPrivilege(systemContext.domains().get(type.getName()));
			}
		}
		return false;
	}

	@Override
	public boolean hasWriteAccess(CMPrivilegedObject privilegedObject) {
		if (privilegedObject instanceof CMEntryType) {
			final CMEntryType type = (CMEntryType) privilegedObject;
			if (type instanceof CMClass) {
				return pm.hasWritePrivilege(systemContext.tables().get(type.getName()));
			} else {
				return pm.hasWritePrivilege(systemContext.domains().get(type.getName()));
			}
		}
		return false;
	}

	@Override
	public boolean hasAdministratorPrivileges() {
		return pm.isAdmin();
	}

	@Override
	public boolean hasDatabaseDesignerPrivileges() {
		return pm.isAdmin();
	}

	@Override
	public boolean hasPrivilege(CMPrivilege privilege) {
		return pm.isAdmin();
	}

	@Override
	public Iterable<PrivilegePair> getAllPrivileges() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean hasPrivilege(CMPrivilege requested, CMPrivilegedObject privilegedObject) {
		if (requested == DefaultPrivileges.READ) {
			return hasReadAccess(privilegedObject);
		}
		if (requested == DefaultPrivileges.WRITE) {
			return hasWriteAccess(privilegedObject);
		}
		return pm.isAdmin();
	}
}
