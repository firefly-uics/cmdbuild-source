package org.cmdbuild.services.auth;

import org.cmdbuild.auth.acl.CMPrivilegedObject;
import org.cmdbuild.elements.wrappers.PrivilegeCard.PrivilegeType;

public interface PrivilegeManager {

	boolean isAdmin();

	void assureAdminPrivilege();

	void assureReadPrivilege(CMPrivilegedObject table);

	boolean hasReadPrivilege(CMPrivilegedObject table);

	void assureWritePrivilege(CMPrivilegedObject table);

	boolean hasWritePrivilege(CMPrivilegedObject table);

	void assureCreatePrivilege(CMPrivilegedObject table);

	boolean hasCreatePrivilege(CMPrivilegedObject table);

	PrivilegeType getPrivilege(CMPrivilegedObject schema);

}
