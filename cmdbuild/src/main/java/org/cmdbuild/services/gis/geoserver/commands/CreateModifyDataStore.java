package org.cmdbuild.services.gis.geoserver.commands;

import java.io.InputStream;

import org.cmdbuild.utils.Command;
import org.cmdbuild.services.gis.geoserver.GeoServerStore;
import org.cmdbuild.services.gis.geoserver.GeoServerStore.StoreDataType;

public class CreateModifyDataStore extends AbstractGeoCommand implements Command<Void> {

	private final GeoServerStore store;
	private InputStream data;

	public static Void exec(final GeoServerStore store, InputStream data) {
		return new CreateModifyDataStore(store, data).run();
	}

	private CreateModifyDataStore(final GeoServerStore store, InputStream data) {
		super();
		this.store = store;
		this.data = data;
	}

	@Override
	public Void run() {
		final StoreDataType type = store.getDataType();
		final String url = String.format("%s/rest/workspaces/%s/%ss/%s/file.%s",
				getGeoServerURL(), getGeoServerWorkspace(), type.getStoreTypeName().toLowerCase(), store.getName(), type.getUploadFileExtension());
		put(data, url, type.getMime());
		return null;
	}
}
