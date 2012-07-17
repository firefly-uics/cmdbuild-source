package org.cmdbuild.api.fluent.ws;

import org.cmdbuild.api.fluent.CardCreator;
import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.services.soap.Private;

public class WsFluentApi implements FluentApi {

	private final Private proxy;

	public WsFluentApi(final Private proxy) {
		this.proxy = proxy;
	}

	public CardCreator newCard() {
		return new WsCardCreator(proxy);
	}

}
