package org.cmdbuild.logic.auth;

import org.cmdbuild.auth.ClientRequestAuthenticator.ClientRequest;
import org.cmdbuild.auth.ForwardingUserStore;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.user.OperationUser;

public class DefaultSessionLogic extends ForwardingAuthenticationLogic implements SessionLogic {

	private final AuthenticationLogic delegate;
	private final UserStore userStore;

	public DefaultSessionLogic(final AuthenticationLogic delegate, final UserStore userStore) {
		this.delegate = delegate;
		this.userStore = userStore;
	}

	@Override
	protected AuthenticationLogic delegate() {
		return delegate;
	}

	@Override
	public boolean isValidUser() {
		return userStore.getUser().isValid();
	}

	@Override
	public ClientAuthenticationResponse login(final ClientRequest request) {
		return delegate().login(request, new ForwardingUserStore() {

			@Override
			protected UserStore delegate() {
				return userStore;
			}

			@Override
			public void setUser(final OperationUser user) {
				// TODO create session
				super.setUser(user);
			}

		});
	}

}
