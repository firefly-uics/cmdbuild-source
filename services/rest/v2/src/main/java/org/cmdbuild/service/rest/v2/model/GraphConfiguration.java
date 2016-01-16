package org.cmdbuild.service.rest.v2.model;

import static org.cmdbuild.service.rest.v2.constants.Serialization.BASE_LEVEL;
import static org.cmdbuild.service.rest.v2.constants.Serialization.ENABLED;
import static org.cmdbuild.service.rest.v2.constants.Serialization.EXPANDING_THRESHOLD;
import static org.cmdbuild.service.rest.v2.constants.Serialization.EXTENSION_MAXIMUM_LEVEL;
import static org.cmdbuild.service.rest.v2.constants.Serialization.CLUSTERING_THRESHOLD;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
public class GraphConfiguration extends AbstractModel {

	private boolean enabled;
	private int baseLevel;
	private int expandingThreshold;
	private int clusteringThreshold;
	private int extensionMaximumLevel;

	GraphConfiguration() {
		// package visibility
	}

	@XmlAttribute(name = ENABLED)
	public boolean isEnabled() {
		return enabled;
	}

	void setEnabled(final boolean value) {
		this.enabled = value;
	}

	@XmlAttribute(name = BASE_LEVEL)
	public int getBaseLevel() {
		return baseLevel;
	}

	void setBaseLevel(final int value) {
		this.baseLevel = value;
	}

	@XmlAttribute(name = EXPANDING_THRESHOLD)
	public int getExpandingThreshold() {
		return expandingThreshold;
	}

	void setExpandingThreshold(final int value) {
		this.expandingThreshold = value;
	}

	@XmlAttribute(name = CLUSTERING_THRESHOLD)
	public int getClusteringThreshold() {
		return clusteringThreshold;
	}

	void setClusteringThreshold(final int value) {
		this.clusteringThreshold = value;
	}

	@XmlAttribute(name = EXTENSION_MAXIMUM_LEVEL)
	public int getExtensionMaximumLevel() {
		return extensionMaximumLevel;
	}

	public void setExtensionMaximumLevel(final int extensionMaximumLevel) {
		this.extensionMaximumLevel = extensionMaximumLevel;
	}

	@Override
	protected boolean doEquals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof LongId)) {
			return false;
		}

		final GraphConfiguration other = GraphConfiguration.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.enabled, other.enabled) //
				.append(this.baseLevel, other.baseLevel) //
				.append(this.expandingThreshold, other.expandingThreshold) //
				.append(this.clusteringThreshold, other.clusteringThreshold) //
				.append(this.extensionMaximumLevel, other.extensionMaximumLevel) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(this.enabled) //
				.append(this.baseLevel) //
				.append(this.expandingThreshold) //
				.append(this.clusteringThreshold) //
				.append(this.extensionMaximumLevel) //
				.toHashCode();
	}

}
