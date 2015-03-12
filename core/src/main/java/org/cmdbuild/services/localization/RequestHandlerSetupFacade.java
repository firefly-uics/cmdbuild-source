package org.cmdbuild.services.localization;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import org.cmdbuild.logic.translation.SetupFacade;
import org.cmdbuild.logic.translation.SetupFacade.ForwardingSetupFacade;

public class RequestHandlerSetupFacade extends ForwardingSetupFacade implements RequestHandler {

	private static final ThreadLocal<Boolean> store = new ThreadLocal<Boolean>();

	private final SetupFacade delegate;

	public RequestHandlerSetupFacade(final SetupFacade delegate) {
		this.delegate = delegate;
	}

	@Override
	protected SetupFacade delegate() {
		return delegate;
	};

	@Override
	public boolean isEnabled() {
		return super.isEnabled() && defaultIfNull(store.get(), false);
	}

	@Override
	public void setLocalized(final boolean value) {
		store.set(value);
	}

}
