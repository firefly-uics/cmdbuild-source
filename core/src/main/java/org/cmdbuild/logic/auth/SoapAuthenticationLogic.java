package org.cmdbuild.logic.auth;

public class SoapAuthenticationLogic extends ForwardingAuthenticationLogic {

	private final AuthenticationLogic delegate;

	public SoapAuthenticationLogic(final AuthenticationLogic delegate) {
		this.delegate = delegate;
	}

	@Override
	protected AuthenticationLogic delegate() {
		return delegate;
	}

}
