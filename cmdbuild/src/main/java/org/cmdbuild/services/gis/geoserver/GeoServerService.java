package org.cmdbuild.services.gis.geoserver;

import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;

import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.gis.LayerMetadata;
import org.cmdbuild.services.gis.geoserver.GeoServerStore.StoreDataType;
import org.cmdbuild.services.gis.geoserver.commands.CreateModifyDataStore;
import org.cmdbuild.services.gis.geoserver.commands.DeleteFeatureTypeOrCoverage;
import org.cmdbuild.services.gis.geoserver.commands.DeleteLayer;
import org.cmdbuild.services.gis.geoserver.commands.DeleteStore;
import org.cmdbuild.services.gis.geoserver.commands.ListLayers;
import org.cmdbuild.services.gis.geoserver.commands.ListStores;

public class GeoServerService {

	public List<GeoServerStore> getStores() {
		return ListStores.exec();
	}

	public List<LayerMetadata> getLayers() {
		return ListLayers.exec();
	}

	public synchronized String createStoreAndLayer(final LayerMetadata layerMetadata, InputStream data) {
		if (nameIsNotValid(layerMetadata.getName())) {
			throw new IllegalArgumentException(
					String.format("Layer name must match regex \"%s\"", namePattern.toString())
				);
		}

		GeoServerStore s = new GeoServerStore(layerMetadata.getName(), StoreDataType.valueOf(layerMetadata.getType())); 
		return CreateModifyDataStore.exec(s, data);
	}

	public synchronized void modifyStoreData(final LayerMetadata layerMetadata, InputStream data) {
		GeoServerStore s = new GeoServerStore(layerMetadata.getName(), StoreDataType.valueOf(layerMetadata.getType()));
		CreateModifyDataStore.exec(s, data);
	}

	public synchronized void deleteStoreAndLayers(final LayerMetadata layer) {
		StoreDataType storeDatatype = StoreDataType.valueOf(layer.getType());
		GeoServerStore store = new GeoServerStore(layer.getName(), storeDatatype);

		try {
			// Delete the layer first because the store
			// must be empty to be deleted
			List<LayerMetadata> storeLayers = ListLayers.exec(store.getName());
			for (LayerMetadata geoServerLayer: storeLayers) {
				DeleteLayer.exec(geoServerLayer);
				DeleteFeatureTypeOrCoverage.exec(geoServerLayer, store);
			}
		} catch (NotFoundException e) {
			Log.OTHER.warn(String.format("GeoServer layer for store %s not found", layer.getName()));
		}

		DeleteStore.exec(store);
	}

	private static final Pattern namePattern = java.util.regex.Pattern.compile("^\\S+$");

	private boolean nameIsNotValid(final String name) {
		return !namePattern.matcher(name).matches();
	}
}
