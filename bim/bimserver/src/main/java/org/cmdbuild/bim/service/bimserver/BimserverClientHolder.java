package org.cmdbuild.bim.service.bimserver;

import org.bimserver.client.BimServerClient;
import org.bimserver.client.BimServerClientFactory;
import org.bimserver.client.soap.SoapBimServerClientFactory;
import org.bimserver.shared.UsernamePasswordAuthenticationInfo;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.bimserver.BimserverConfiguration.ChangeListener;
import org.cmdbuild.common.Holder;

public class BimserverClientHolder implements Holder<BimServerClient>, ChangeListener {

	private BimServerClient client;
	private final BimserverConfiguration configuration;
	private boolean clientValid;

	public BimserverClientHolder(final BimserverConfiguration configuration) {
		this.configuration = configuration;
		this.configuration.addListener(this);
		buildClient();
	}

	@Override
	public BimServerClient get() {
		return clientValid ? client : null;
	}

	@Override
	public void configurationChanged() {
		buildClient();
	}

	private void buildClient() {
		if (configuration.isEnabled()) {
			BimServerClientFactory factory = new SoapBimServerClientFactory(configuration.getUrl());
			try {
				client = factory.create(new UsernamePasswordAuthenticationInfo(configuration.getUsername(),
						configuration.getPassword()));
				clientValid = true;
			} catch (final Throwable t) {
				throw new BimError("Unable to connect to '" + configuration.getUrl()
						+ "'. Please check configuration parameters.", t);
			}
		} else {
			clientValid = false;
		}
	}

}
