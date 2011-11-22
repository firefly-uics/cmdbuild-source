package org.cmdbuild.auth.user;

import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.CMPrivilegedObject;

/**
 * Helper class to implement specializations of OperationUser
 */
public abstract class OperationUserWrapper implements OperationUser {

	protected final OperationUser inner;

	protected OperationUserWrapper(final OperationUser inner) {
		this.inner = inner;
	}

	@Override
	public final String getOperationUsername() {
		return inner.getOperationUsername();
	}

	@Override
	public final boolean hasReadAccess(CMPrivilegedObject privilegedObject) {
		return inner.hasReadAccess(privilegedObject);
	}

	@Override
	public final boolean hasWriteAccess(CMPrivilegedObject privilegedObject) {
		return inner.hasWriteAccess(privilegedObject);
	}

	@Override
	public final boolean hasPrivilege(CMPrivilege privilege) {
		return inner.hasPrivilege(privilege);
	}
}