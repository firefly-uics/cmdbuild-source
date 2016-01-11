package org.cmdbuild.config;

public interface GraphConfiguration {

	boolean isEnabled();

	int getBaseLevel();

	int getExtensionMaximumLevel();

	int getClusteringThreshold();

	int getExpandingThreshold();

}
