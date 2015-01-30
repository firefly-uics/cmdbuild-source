package org.cmdbuild.auth;

import org.cmdbuild.auth.user.OperationUser;

public abstract class ForwardingUserStore implements UserStore {

	private final UserStore delegate;

	protected ForwardingUserStore(final UserStore delegate) {
		this.delegate = delegate;
	}

	@Override
	public OperationUser getUser() {
		return delegate.getUser();
	}

	@Override
	public void setUser(final OperationUser user) {
		delegate.setUser(user);
	}

}
