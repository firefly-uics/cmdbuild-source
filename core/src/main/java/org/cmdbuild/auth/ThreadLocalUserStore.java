package org.cmdbuild.auth;

import org.cmdbuild.auth.user.OperationUser;

public class ThreadLocalUserStore extends ForwardingUserStore {

	private static final ThreadLocal<OperationUser> threadLocal = new ThreadLocal<OperationUser>();

	public ThreadLocalUserStore(final UserStore delegate) {
		super(delegate);
	}

	@Override
	public OperationUser getUser() {
		OperationUser user = threadLocal.get();
		if (user == null) {
			user = super.getUser();
		}
		return user;
	}

	@Override
	public void setUser(final OperationUser user) {
		threadLocal.set(user);
		super.setUser(user);
	}

}
