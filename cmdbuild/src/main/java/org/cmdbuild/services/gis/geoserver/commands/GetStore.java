package org.cmdbuild.services.gis.geoserver.commands;

import org.cmdbuild.services.gis.geoserver.GeoServerStore;
import org.cmdbuild.services.gis.geoserver.GeoServerStore.StoreDataType;
import org.cmdbuild.services.gis.geoserver.GeoServerStore.StoreType;
import org.cmdbuild.utils.Command;
import org.dom4j.Document;

public class GetStore extends AbstractGeoCommand implements Command<GeoServerStore> {

	private final String name;
	private final StoreType type;

	public static GeoServerStore exec(final String name, final StoreType type) {
		return new GetStore(name, type).run();
	}

	private GetStore(final String name, final StoreType type) {
		super();
		this.name = name;
		this.type = type;
	}

	@Override
	public GeoServerStore run() {
		final String url = String.format("%s/rest/workspaces/%s/%ss/%s",
				getGeoServerURL(), getGeoServerWorkspace(),
				type.getName().toLowerCase(), name);
		final Document xmlLayer = get(url);
		StoreDataType dataType = extractDataType(xmlLayer);
		return new GeoServerStore(name, dataType);
	}

	private StoreDataType extractDataType(final Document xmlLayer) {
		String dataTypeName;
		try {
			final String xpathExpression = String.format("//%s/type", type.getName());
			dataTypeName = xmlLayer.valueOf(xpathExpression);
		} catch (Exception e) {
			dataTypeName = null;
		}
		return getStoreDataTypeBySubtype(dataTypeName);
	}

	private StoreDataType getStoreDataTypeBySubtype(String subtype) {
		for (StoreDataType dt : StoreDataType.values()) {
			if (dt.getStoreSubtype().equals(subtype)) {
				return dt;
			}
		}
		return StoreDataType.SHAPE;
	}
}
