package org.cmdbuild.api.fluent.ws;

import org.cmdbuild.services.soap.Private;

abstract class WsExecutor {

	private final Private proxy;

	public WsExecutor(final Private proxy) {
		this.proxy = proxy;
	}

	protected Private proxy() {
		return proxy;
	}

}
