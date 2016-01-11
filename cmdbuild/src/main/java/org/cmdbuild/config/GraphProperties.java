package org.cmdbuild.config;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;

import org.cmdbuild.services.Settings;

public class GraphProperties extends DefaultProperties implements GraphConfiguration {

	private static final long serialVersionUID = 1L;

	private static final String MODULE_NAME = "graph";

	private static final String BASE_LEVEL = "baseLevel";
	private static final String ENABLED = "enabled";
	private static final String EXTENSION_MAXIMUM_LEVEL = "extensionMaximumLevel";
	private static final String CLUSTERING_THRESHOLD = "clusteringThreshold";
	private static final String EXPANDING_THRESHOLD = "expandingThreshold";

	public GraphProperties() {
		super();
		setProperty(BASE_LEVEL, "1");
		setProperty(EXTENSION_MAXIMUM_LEVEL, "5");
		setProperty(CLUSTERING_THRESHOLD, "5");
		setProperty(EXPANDING_THRESHOLD, "30");
		setProperty(ENABLED, Boolean.TRUE.toString());
	}

	public static GraphProperties getInstance() {
		return (GraphProperties) Settings.getInstance().getModule(MODULE_NAME);
	}

	@Override
	public boolean isEnabled() {
		return parseBoolean(getProperty(ENABLED));
	}

	@Override
	public int getBaseLevel() {
		return parseInt(getProperty(BASE_LEVEL));
	}

	@Override
	public int getExtensionMaximumLevel() {
		return parseInt(getProperty(EXTENSION_MAXIMUM_LEVEL));
	}

	@Override
	public int getClusteringThreshold() {
		return parseInt(getProperty(CLUSTERING_THRESHOLD));
	}

	@Override
	public int getExpandingThreshold() {
		return parseInt(getProperty(EXPANDING_THRESHOLD));
	}

}
