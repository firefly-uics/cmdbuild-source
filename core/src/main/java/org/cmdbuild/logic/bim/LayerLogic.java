package org.cmdbuild.logic.bim;

import java.util.List;

import org.cmdbuild.logic.Logic;
import org.cmdbuild.model.bim.BimLayer;

public interface LayerLogic extends Logic {

	List<BimLayer> readLayers();
	
	Iterable<BimLayer> getActiveLayers();

	void updateBimLayer(String className, String attributeName, String value);

	BimLayer getRootLayer();

	boolean isActive(String classname);

}
