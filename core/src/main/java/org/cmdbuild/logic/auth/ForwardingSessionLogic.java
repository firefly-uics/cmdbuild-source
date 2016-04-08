package org.cmdbuild.logic.auth;

import org.cmdbuild.auth.ClientRequestAuthenticator.ClientRequest;

public abstract class ForwardingSessionLogic extends ForwardingAuthenticationLogic implements SessionLogic {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingSessionLogic() {
	}

	@Override
	protected abstract SessionLogic delegate();

	@Override
	public boolean isValidUser() {
		return delegate().isValidUser();
	}

	@Override
	public ClientAuthenticationResponse login(final ClientRequest request) {
		return delegate().login(request);
	}

}
