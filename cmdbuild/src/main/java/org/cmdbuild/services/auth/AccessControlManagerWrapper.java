package org.cmdbuild.services.auth;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.view.CMAccessControlManager;

public class AccessControlManagerWrapper implements CMAccessControlManager {

	private final static UserContext systemContext = UserContext.systemContext(); 
	final PrivilegeManager pm;

	public AccessControlManagerWrapper(final PrivilegeManager pm) {
		this.pm = pm;
	}

	@Override
	public boolean hasReadAccess(CMEntryType type) {
		if (type instanceof CMClass) {
			return pm.hasReadPrivilege(systemContext.tables().get(type.getName()));
		} else {
			return pm.hasReadPrivilege(systemContext.domains().get(type.getName()));
		}
	}

	@Override
	public boolean hasDatabaseDesignerPrivileges() {
		return pm.isAdmin();
	}

	@Override
	public boolean hasWorkflowDesignerPrivileges() {
		return pm.isAdmin();
	}

	@Override
	public boolean hasAdministratorPrivileges() {
		return pm.isAdmin();
	}

	@Override
	public boolean hasReportDesignerPrivileges() {
		return pm.isAdmin();
	}
}
