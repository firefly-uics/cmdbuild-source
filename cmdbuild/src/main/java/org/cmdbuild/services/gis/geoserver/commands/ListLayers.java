package org.cmdbuild.services.gis.geoserver.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cmdbuild.model.gis.LayerMetadata;
import org.cmdbuild.utils.Command;
import org.dom4j.Node;

public class ListLayers extends AbstractGeoCommand implements Command<List<LayerMetadata>> {

	/**
	 * if there is a store name, the command return only the layers of that store
	 */
	private String storeName;

	public static List<LayerMetadata> exec() {
		return new ListLayers().run();
	}

	public static List<LayerMetadata> exec(String storeName) {
		return new ListLayers(storeName).run();
	}

	private ListLayers() {
		this(null);
	}

	private ListLayers(String storeName) {
		super();
		this.storeName = storeName;
	}

	@Override
	public List<LayerMetadata> run() {
		List<LayerMetadata> layers = new ArrayList<LayerMetadata>();

		final String url = String.format("%s/rest/layers", getGeoServerURL());

		List<?> layerList = get(url).selectNodes("//layers/layer");
		for (Iterator<?> iter = layerList.iterator(); iter.hasNext(); ) {
			String layerName = ((Node) iter.next()).valueOf("name");
			LayerMetadata layer = GetLayer.exec(layerName);
			if (this.storeName != null 
					&& this.storeName.equals(layer.getStoreName())) {
				layers.add(layer);
			}
		}

		return layers;
	}
}
