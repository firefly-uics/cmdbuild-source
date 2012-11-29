package org.cmdbuild.services.gis.geoserver.commands;

import java.io.InputStream;
import java.util.List;

import org.cmdbuild.services.gis.geoserver.GeoServerLayer;
import org.cmdbuild.services.gis.geoserver.GeoServerStore;
import org.cmdbuild.services.gis.geoserver.GeoServerStore.StoreDataType;
import org.cmdbuild.utils.Command;

public class CreateModifyDataStore extends AbstractGeoCommand implements Command<String> {

	private final GeoServerStore store;
	private InputStream data;

	public static String exec(final GeoServerStore store, InputStream data) {
		return new CreateModifyDataStore(store, data).run();
	}

	private CreateModifyDataStore(final GeoServerStore store, InputStream data) {
		super();
		this.store = store;
		this.data = data;
	}

	@Override
	public String run() {
		final StoreDataType type = store.getDataType();
		final String url = String.format("%s/rest/workspaces/%s/%ss/%s/file.%s",
				getGeoServerURL(), getGeoServerWorkspace(), type.getStoreTypeName().toLowerCase(), store.getName(), type.getUploadFileExtension());
		put(data, url, type.getMime());
		List<GeoServerLayer> storeLayers = ListLayers.exec(store.getName());

		if (storeLayers.size() > 0) {
			GeoServerLayer l = storeLayers.get(0);
			return l.getName();
		}

		return null;
	}
}
