package org.cmdbuild.service.rest.v2.model;

import static org.cmdbuild.service.rest.v2.constants.Serialization.BASE_LEVEL;
import static org.cmdbuild.service.rest.v2.constants.Serialization.CLUSTERING_THRESHOLD;
import static org.cmdbuild.service.rest.v2.constants.Serialization.ENABLED;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@XmlRootElement
public class GraphConfiguration extends AbstractModel {

	private boolean enabled;
	private int baseLevel;
	private int clusteringThreshold;

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

	@XmlAttribute(name = CLUSTERING_THRESHOLD)
	public int getClusteringThreshold() {
		return clusteringThreshold;
	}

	void setClusteringThreshold(final int value) {
		this.clusteringThreshold = value;
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
				.append(this.clusteringThreshold, other.clusteringThreshold) //
				.isEquals();
	}

	@Override
	protected int doHashCode() {
		return new HashCodeBuilder() //
				.append(this.enabled) //
				.append(this.baseLevel) //
				.append(this.clusteringThreshold) //
				.toHashCode();
	}

}
