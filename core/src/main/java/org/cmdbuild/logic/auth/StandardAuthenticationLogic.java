package org.cmdbuild.logic.auth;

public class StandardAuthenticationLogic extends ForwardingAuthenticationLogic {

	private final AuthenticationLogic delegate;

	public StandardAuthenticationLogic(final AuthenticationLogic delegate) {
		this.delegate = delegate;
	}

	@Override
	protected AuthenticationLogic delegate() {
		return delegate;
	}

}
