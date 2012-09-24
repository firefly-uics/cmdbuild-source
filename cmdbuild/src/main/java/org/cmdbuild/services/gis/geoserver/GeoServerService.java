package org.cmdbuild.services.gis.geoserver;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.gis.GeoLayer;
import org.cmdbuild.services.gis.LayerService;
import org.cmdbuild.services.gis.geoserver.GeoServerStore.StoreDataType;
import org.cmdbuild.services.gis.geoserver.commands.CreateModifyDataStore;
import org.cmdbuild.services.gis.geoserver.commands.DeleteFeatureTypeOrCoverage;
import org.cmdbuild.services.gis.geoserver.commands.DeleteLayer;
import org.cmdbuild.services.gis.geoserver.commands.DeleteStore;
import org.cmdbuild.services.gis.geoserver.commands.ListLayers;
import org.cmdbuild.services.gis.geoserver.commands.ListStores;
import org.cmdbuild.services.gis.geoserver.commands.ModifyLayer;

public class GeoServerService implements LayerService {

	private static Object cacheLock = new Object();
	private static volatile List<GeoServerStore> stores;
	private static volatile List<GeoServerLayer> layers;
	private static volatile long lastCacheUpdate = 0;
	private static volatile boolean lastUpdateFailed = false;
	private static final long CACHE_UPDATE_INTERVAL_MS = 5*(1000*60); // Every 5 minutes
	private static volatile long CACHE_UPDATE_ON_UNREACHABLE_MS = 30*1000; // 30 seconds timeout if unreachable

	public List<GeoServerStore> getStores() {
		updateCacheIfTimedout();
		return stores;
	}

	@Override
	public List<GeoServerLayer> getLayers() {
		updateCacheIfTimedout();
		return layers;
	}

	@Override
	public GeoServerLayer getLayer(final String name, final ITable masterTable) {
		if (masterTable == null) {
			return getLayer(name);
		}
		throw NotFoundExceptionType.NOTFOUND.createException();
	}

	@Override
	public synchronized void setLayerVisibility(final String name, final ITable masterTable, final ITable table, final boolean visible) {
		GeoServerLayer l = getLayer(name, masterTable);
		l.setVisibility(table, visible);
		saveLayer(l);
	}

	public void setLayerPosition(GeoLayer layer, int position) {
		GeoServerLayer l = getLayer(layer.getName());
		l.setIndex(position);
		saveLayer(l);
	}

	private void updateCacheIfTimedout() {
		synchronized (cacheLock) {
			long now = new Date().getTime();
			if (hasTimedOut(now) || hasFailedAndTimedOut(now)) {
				Log.OTHER.info("Updating Geoserver cache");
				try {
					stores = ListStores.exec();
					layers = ListLayers.exec();
					lastUpdateFailed = false;
				} catch (NotFoundException e) {
					stores = new ArrayList<GeoServerStore>();
					layers = new ArrayList<GeoServerLayer>();
					lastUpdateFailed = true;
					if (e.getExceptionType() != NotFoundExceptionType.SERVICE_UNAVAILABLE) {
						throw e;
					} else {
						if (Log.OTHER.isDebugEnabled()) {
							Log.OTHER.error("Geoserver unreachable!", e);
						} else {
							Log.OTHER.error("Geoserver unreachable!");
						}
					}
				}
				lastCacheUpdate = now;
			}
		}
	}

	private boolean hasFailedAndTimedOut(long now) {
		return lastUpdateFailed && (now - lastCacheUpdate > CACHE_UPDATE_ON_UNREACHABLE_MS);
	}
	
	private boolean hasTimedOut(long now) {
		return (now - lastCacheUpdate > CACHE_UPDATE_INTERVAL_MS);
	}

	// TODO: Add new data to the cache instead of invalidating it
	private void invalidateCache() {
		lastCacheUpdate = 0;
	}

	public synchronized void createStore(final String name, final String dataType, InputStream data,
			int minZoom, int maxZoom, int position, final String description) {
		if (nameIsNotValid(name)) {
			throw new IllegalArgumentException(
					String.format("Layer name must match regex \"%s\"", namePattern.toString())
				);
		}
		GeoServerStore s = new GeoServerStore(name, StoreDataType.valueOf(dataType)); 
		CreateModifyDataStore.exec(s, data);
		invalidateCache();
		GeoServerLayer l = getLayerForStore(name);
		l.setDescription(description);
		l.setMinZoom(minZoom);
		l.setMaxZoom(maxZoom);
		l.setIndex(position);
		saveLayer(l);
	}

	public synchronized void modifyStoreData(final String name, InputStream data) {
		GeoServerStore s = getStore(name);
		CreateModifyDataStore.exec(s, data);
	}

	public synchronized void modifyStoreIndex(final String name, int index) {
		GeoServerLayer l = getLayer(name);
		l.setIndex(index);
		saveLayer(l);
	}

	public synchronized void modifyStoreZoomAndDescription(final String name,
			final int minZoom, final int maxZoom, final String description) {
		GeoServerLayer l = getLayer(name);
		l.setDescription(description);
		l.setMinZoom(minZoom);
		l.setMaxZoom(maxZoom);
		saveLayer(l);
	}

	public synchronized void deleteStore(final String name) {
		GeoServerStore s = getStore(name);
		try {
			GeoServerLayer l = getLayerForStore(name);
			DeleteLayer.exec(l);
			DeleteFeatureTypeOrCoverage.exec(l, s);
		} catch (NotFoundException e) {
			Log.OTHER.warn(String.format("GeoServer layer for store %s not found", name));
		}
		DeleteStore.exec(s);
		invalidateCache();
	}

	private GeoServerStore getStore(final String name) {
		for (GeoServerStore s : getStores()) {
			if (s.getName().equals(name)) {
				return s;
			}
		}
		throw NotFoundExceptionType.NOTFOUND.createException();
	}

	public String getStoreTypeName(final String name) {
		return getStore(name).getDataType().toString();
	}

	private GeoServerLayer getLayerForStore(final String storeName) {
		for (GeoServerLayer l : getLayers()) {
			if (storeName.equals(l.getStoreName())) {
				return l;
			}
		}
		throw NotFoundExceptionType.NOTFOUND.createException();
	}

	private GeoServerLayer getLayer(final String storeName) {
		for (GeoServerLayer l : getLayers()) {
			if (storeName.equals(l.getName())) {
				return l;
			}
		}
		throw NotFoundExceptionType.NOTFOUND.createException();
	}

	private void saveLayer(GeoServerLayer l) {
		ModifyLayer.exec(l);
		invalidateCache();
	}

	private static final Pattern namePattern = java.util.regex.Pattern.compile("^\\S+$");

	private boolean nameIsNotValid(final String name) {
		return !namePattern.matcher(name).matches();
	}
}
