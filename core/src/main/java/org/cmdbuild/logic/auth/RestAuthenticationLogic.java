package org.cmdbuild.logic.auth;

public class RestAuthenticationLogic extends ForwardingAuthenticationLogic {

	private final AuthenticationLogic delegate;

	public RestAuthenticationLogic(final AuthenticationLogic delegate) {
		this.delegate = delegate;
	}

	@Override
	protected AuthenticationLogic delegate() {
		return delegate;
	}

}
