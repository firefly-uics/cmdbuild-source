package org.cmdbuild.services.gis.geoserver.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cmdbuild.services.gis.geoserver.GeoServerLayer;
import org.cmdbuild.utils.Command;
import org.dom4j.Node;

public class ListLayers extends AbstractGeoCommand implements Command<List<GeoServerLayer>> {

	public static List<GeoServerLayer> exec() {
		return new ListLayers().run();
	}

	private ListLayers() {
		super();
	}

	@Override
	public List<GeoServerLayer> run() {
		List<GeoServerLayer> layers = new ArrayList<GeoServerLayer>();

		final String url = String.format("%s/rest/layers", getGeoServerURL());

        List<?> layerList = get(url).selectNodes("//layers/layer");
        for (Iterator<?> iter = layerList.iterator(); iter.hasNext(); ) {
        	String layerName = ((Node) iter.next()).valueOf("name");
        	GeoServerLayer layer = GetLayer.exec(layerName);
			layers.add(layer);
        }

        return layers;
	}
}
