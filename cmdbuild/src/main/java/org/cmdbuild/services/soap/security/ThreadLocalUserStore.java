package org.cmdbuild.services.soap.security;

import static org.cmdbuild.auth.user.AuthenticatedUserImpl.ANONYMOUS_USER;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.context.NullPrivilegeContext;
import org.cmdbuild.auth.user.OperationUser;

public class ThreadLocalUserStore implements UserStore {

	private static final ThreadLocal<OperationUser> holder = new ThreadLocal<OperationUser>();

	@Override
	public OperationUser getUser() {
		OperationUser operationUser = holder.get();
		if (operationUser == null) {
			operationUser = new OperationUser(ANONYMOUS_USER, new NullPrivilegeContext(), new NullGroup());
			setUser(operationUser);
		}
		return operationUser;
	}

	@Override
	public void setUser(final OperationUser user) {
		holder.set(user);
	}

}
