package org.cmdbuild.services.gis.geoserver;

import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.services.gis.AbstractGeoLayer;

public class GeoServerLayer extends AbstractGeoLayer {

	public static final String DESCRIPTION_META = "cmdbuild.description";
	public static final String INDEX_META = "cmdbuild.index";
	public static final String MIN_ZOOM_META = "cmdbuild.minZoom";
	public static final String MAX_ZOOM_META = "cmdbuild.maxZoom";
	public static final String VISIBILITY_META = "cmdbuild.visibility";

	private final String storeName;
	private String description;

	public GeoServerLayer(String name, String description, String dsName) {
		super(name);
		this.storeName = dsName;
		if (description == null || description.isEmpty()) {
			this.description = name;
		} else {
			this.description = description;
		}
	}

	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isLocal(ITable table) {
		return false;
	}

	public String getStoreName() {
		return storeName;
	}

	public String toString() {
		return getStoreName() + "/" + getName();
	}

	// FIXME Horrible
	@Override
	public String getTypeName() {
		return new GeoServerService().getStoreTypeName(getStoreName());
	}
}
