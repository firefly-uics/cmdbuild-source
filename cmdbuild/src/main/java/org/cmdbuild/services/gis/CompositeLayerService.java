package org.cmdbuild.services.gis;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.config.GisProperties;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.services.gis.GeoFeatureType.GeoType;
import org.cmdbuild.services.gis.geoserver.GeoServerService;
import org.cmdbuild.utils.OrderingUtils;
import org.cmdbuild.utils.OrderingUtils.PositionHandler;

public class CompositeLayerService implements LayerService {

	private static Object layersLock = new Object();
	private static GeoServerService geoServerService = new GeoServerService();

	@Override
	public List<? extends GeoLayer> getLayers() {
		List<GeoLayer> layers = new ArrayList<GeoLayer>();
		if (isGisEnabled()) {
			layers.addAll(GeoFeatureType.list());
			if (isGeoServerEnabled()) {
				layers.addAll(geoServerService.getLayers());
			}
		}
		return layers;
	}

	@Override
	public GeoLayer getLayer(String name, ITable masterTable) {
		if (isGisEnabled()) {
			if (masterTable != null) {
				return new GeoTable(masterTable).getGeoFeatureType(name);
			} else if (isGeoServerEnabled()) {
				return geoServerService.getLayer(name, masterTable);
			}
		}
		throw NotFoundExceptionType.NOTFOUND.createException();
	}

	@Override
	public void setLayerVisibility(String name, ITable masterTable, ITable table, boolean visible) {
		if (isGisEnabled()) {
			if (masterTable != null) {
				GeoFeatureType l = new GeoTable(masterTable).getGeoFeatureType(name);
				l.setVisibility(table, visible);
				l.save();
			} else if (isGeoServerEnabled()) {
				geoServerService.setLayerVisibility(name, masterTable, table, visible);
			}
		} else {
			throw NotFoundExceptionType.SERVICE_UNAVAILABLE.createException();
		}
	}

	private boolean isGisEnabled() {
		return GisProperties.getInstance().isEnabled();
	}

	private boolean isGeoServerEnabled() {
		return GisProperties.getInstance().isGeoServerEnabled();
	}

	private void setLayerPosition(GeoLayer layer, int position) {
		if (layer instanceof GeoFeatureType) {
			GeoFeatureType gft = (GeoFeatureType) layer;
			gft.setIndex(position);
			gft.save();
		} else {
			geoServerService.setLayerPosition(layer, position);
		}
	}

	public void reorderLayers(int oldIndex, int newIndex) {
		synchronized (layersLock) {
			OrderingUtils.alterPosition(getLayers(), oldIndex, newIndex, new PositionHandler<GeoLayer>() {
				@Override
				public int getPosition(GeoLayer l) {
					return l.getIndex();
				}
				@Override
				public void setPosition(GeoLayer l, int p) {
					setLayerPosition(l, p);
				}
			});
		}
	}

	public GeoFeatureType createGeoFeatureType(ITable master, String name, String description, GeoType geoType,
			int minZoom, int maxZoom, String style) {
		synchronized (layersLock) {
			int position = getLayers().size();
			GeoTable geoMasterClass = new GeoTable(master);
			GeoFeatureType gft = geoMasterClass.createGeoFeatureType(name, description, geoType,
					minZoom, maxZoom, style, position);
			return gft;
		}
	}

	public void createGeoServerLayer(final String name, final String dataType, InputStream data,
			final int minZoom, final int maxZoom, final String description) {
		synchronized (layersLock) {
			int position = getLayers().size();
			geoServerService.createStore(name, dataType, data, minZoom, maxZoom, position, description);
		}
	}
}
