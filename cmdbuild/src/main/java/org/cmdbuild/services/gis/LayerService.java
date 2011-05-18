package org.cmdbuild.services.gis;

import java.util.List;

import org.cmdbuild.elements.interfaces.ITable;

public interface LayerService {

	List<? extends GeoLayer> getLayers();
	GeoLayer getLayer(String name, ITable masterTable);
	void setLayerVisibility(String name, ITable masterTable, ITable table, boolean visibility);
}
