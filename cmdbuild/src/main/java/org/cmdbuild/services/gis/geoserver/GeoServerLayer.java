package org.cmdbuild.services.gis.geoserver;

import org.cmdbuild.model.gis.LayerMetadata;

public class GeoServerLayer extends LayerMetadata {

	private String storeName;

	public GeoServerLayer(String name, String storeName) {
		super();
		this.setName(name);
		this.storeName = storeName;
	}

	public String getStoreName() {
		return storeName;
	}

	public String toString() {
		return getStoreName() + "/" + getName();
	}

}
