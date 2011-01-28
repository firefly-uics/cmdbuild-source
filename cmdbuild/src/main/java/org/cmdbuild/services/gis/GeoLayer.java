package org.cmdbuild.services.gis;

import org.cmdbuild.elements.interfaces.ITable;

public interface GeoLayer {

	String getName();
	String getDescription();

	String getTypeName();
	boolean isEnabled();

	int getMinZoom();
	void setMinZoom(final int minZoom);
	void setMinZoom(final String minZoom);

	int getMaxZoom();
	void setMaxZoom(final int maxZoom);
	void setMaxZoom(final String maxZoom);

	int getIndex();
	boolean isLocal(ITable table);

	boolean isVisible(ITable table);
	void addVisibility(ITable table);
	void removeVisibility(ITable table);
	void setVisibility(ITable table, boolean visible);
	String getVisibilityAsString();
}
