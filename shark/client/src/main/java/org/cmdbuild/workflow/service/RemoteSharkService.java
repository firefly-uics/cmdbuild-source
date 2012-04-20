package org.cmdbuild.workflow.service;

import java.util.Properties;

import org.enhydra.shark.api.client.wfmc.wapi.WMConnectInfo;

/**
 * Implementation using remote Shark server
 */
public class RemoteSharkService extends AbstractSharkService {

	public interface Config {
		String getServerUrl();
		String getUsername();
		String getPassword();
//		void registerListener(ConfigChangeListener listener);
	}

//	public interface ConfigChangeListener {
//		void nofityConfigChange();
//	}

	private final Config config;

	public RemoteSharkService(final Config config) {
		super(getClientProperties(config));
		this.config = config;
	}

	private static Properties getClientProperties(final Config config) {
		final Properties clientProps = new Properties();
		clientProps.put("ClientType", "WS");
		clientProps.put("SharkWSURLPrefix", config.getServerUrl());
		return clientProps;
	}

	protected WMConnectInfo getConnectionInfo() {
		return new WMConnectInfo(config.getUsername(), config.getPassword(), DEFAULT_ENGINE_NAME, DEFAULT_SCOPE);
	}

}
