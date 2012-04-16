package org.cmdbuild.workflow.service;

import java.util.Properties;

import org.enhydra.shark.api.client.wfmc.wapi.WMConnectInfo;

/**
 * Implementation using remote Shark server
 */
public class LocalSharkService extends AbstractSharkService {

	public interface Config {
		String getUsername();
	}

	private final Config config;

	public LocalSharkService(final Config config) {
		super(getClientProperties());
		this.config = config;
	}

	private static Properties getClientProperties() {
		final Properties clientProps = new Properties();
		clientProps.put("SharkConfResourceLocation", "Shark.conf");
		return clientProps;
	}

	protected WMConnectInfo getConnectionInfo() {
		return new WMConnectInfo(config.getUsername(), "", DEFAULT_ENGINE_NAME, DEFAULT_SCOPE);
	}
}
