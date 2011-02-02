package org.cmdbuild.services.gis.geoserver.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cmdbuild.services.gis.geoserver.GeoServerStore;
import org.cmdbuild.services.gis.geoserver.GeoServerStore.StoreType;
import org.cmdbuild.utils.Command;
import org.dom4j.Node;

public class ListStores extends AbstractGeoCommand implements Command<List<GeoServerStore>> {

	public static List<GeoServerStore> exec() {
		return new ListStores().run();
	}

	private ListStores() {
		super();
	}

	@Override
	public List<GeoServerStore> run() {
		List<GeoServerStore> storeList = new ArrayList<GeoServerStore>();
		for (StoreType storeType : StoreType.values()) {
			addStoresByType(storeList, storeType);
		}
        return storeList;
	}

	private void addStoresByType(List<GeoServerStore> storeList, StoreType storeType) {
		final String url = String.format("%s/rest/workspaces/%s/%ss",
				getGeoServerURL(), getGeoServerWorkspace(), storeType.getName().toLowerCase());
		final String xpathExpression = String.format("//%ss/%s", storeType.getName(), storeType.getName());
        List<?> layerList = get(url).selectNodes(xpathExpression);
        for (Iterator<?> iter = layerList.iterator(); iter.hasNext(); ) {
        	String storeName = ((Node) iter.next()).valueOf("name");
        	GeoServerStore store = GetStore.exec(storeName, storeType);
            storeList.add(store);
        }
	}
}
