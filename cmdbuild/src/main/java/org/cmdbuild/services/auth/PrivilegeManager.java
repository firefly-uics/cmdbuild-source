package org.cmdbuild.services.auth;

import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.wrappers.PrivilegeCard.PrivilegeType;

public interface PrivilegeManager {

	boolean isAdmin();

	void assureAdminPrivilege();

	void assureReadPrivilege(ITable table);
	boolean hasReadPrivilege(ITable table);
	void assureWritePrivilege(ITable table);
	boolean hasWritePrivilege(ITable table);
	void assureCreatePrivilege(ITable table);
	boolean hasCreatePrivilege(ITable table);

	void assureReadPrivilege(IDomain domain);
	boolean hasReadPrivilege(IDomain domain);
	void assureWritePrivilege(IDomain domain);
	boolean hasWritePrivilege(IDomain domain);
	void assureCreatePrivilege(IDomain domain);
	boolean hasCreatePrivilege(IDomain domain);

	boolean hasReadPrivilege(BaseSchema schema);
	PrivilegeType getPrivilege(BaseSchema schema);
}
