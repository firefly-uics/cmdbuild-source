package org.cmdbuild.logic.data.access.lock;

import org.cmdbuild.auth.UserStore;

import com.google.common.base.Supplier;

public class UsernameSupplier implements Supplier<String> {

	private final UserStore store;

	public UsernameSupplier(final UserStore store) {
		this.store = store;
	}

	@Override
	public String get() {
		return store.getUser().getAuthenticatedUser().getUsername();
	}

}
