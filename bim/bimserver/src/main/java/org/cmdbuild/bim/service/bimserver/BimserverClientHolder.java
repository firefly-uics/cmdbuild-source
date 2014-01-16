package org.cmdbuild.bim.service.bimserver;

import org.bimserver.client.BimServerClient;
import org.bimserver.client.BimServerClientFactory;
import org.bimserver.client.soap.SoapBimServerClientFactory;
import org.bimserver.shared.UsernamePasswordAuthenticationInfo;
import org.cmdbuild.bim.service.bimserver.BimserverConfiguration.ChangeListener;
import org.cmdbuild.common.Holder;

public class BimserverClientHolder implements Holder<BimServerClient>, ChangeListener {

	private BimServerClient client;
	private final BimserverConfiguration configuration;
	private boolean connectionValid;

	public BimserverClientHolder(final BimserverConfiguration configuration) {
		this.configuration = configuration;
		this.configuration.addListener(this);
		buildClient();
	}

	@Override
	public BimServerClient get() {
		return ping() ? client : null;
	}

	@Override
	public void configurationChanged() {
		buildClient();
	}

	private boolean ping() {
		boolean pingSuccess = false;
		if (connectionValid) {
			try {
				pingSuccess = client.getBimsie1AuthInterface().isLoggedIn();
			} catch (Throwable t1) {
				buildClient(); // try to reconnect
				if (connectionValid) {
					pingSuccess = true;
				} else {
				}
			}
		} else {
			buildClient(); // try to reconnect
			if (connectionValid) {
				pingSuccess = true;
			}
		}
		return pingSuccess;
	}

	private void buildClient() {
		if (configuration.isEnabled()) {
			BimServerClientFactory factory = new SoapBimServerClientFactory(configuration.getUrl());
			try {
				client = factory.create(new UsernamePasswordAuthenticationInfo(configuration.getUsername(),
						configuration.getPassword()));
				connectionValid = true;
			} catch (final Throwable t) {
				connectionValid = false;
				System.out.println("Connection to BimServer failed");
			}
		} else {
			connectionValid = false;
		}
		if (connectionValid) {
			System.out.println("Connection to BimServer established");
		} 
	}
}