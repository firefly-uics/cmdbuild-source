package org.cmdbuild.services.gis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.proxy.TableForwarder;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.services.gis.GeoFeatureType.GeoType;

public class GeoTable extends TableForwarder {

	// access from getGeoFeatureTypeMap()
	private Map<String, GeoFeatureType> geoFeatureTypeMap;
	private static CompositeLayerService layerService = new  CompositeLayerService();

	public GeoTable(ITable t) {
		super(t);
	}

	private Map<String, GeoFeatureType> getGeoFeatureTypeMap() {
		if (geoFeatureTypeMap == null) {
			geoFeatureTypeMap = buildGeoFeatureTypeMap();
		}
		return geoFeatureTypeMap;
	}

	private Map<String, GeoFeatureType> buildGeoFeatureTypeMap() {
		Map<String, GeoFeatureType> currentGeoFeatureTypeMap = new HashMap<String, GeoFeatureType>();
		for (IAttribute fk : fkDetails()) {
			GeoFeatureType gft = GeoFeatureType.fromGeoTable((ITable)fk.getSchema());
			if (gft != null) {
				currentGeoFeatureTypeMap.put(gft.getName(), gft);
			}
		}
		return currentGeoFeatureTypeMap;
	}

	public Iterable<GeoFeatureType> getGeoFeatureTypes() {
		return getGeoFeatureTypeMap().values();
	}

	public Iterable<GeoLayer> getVisibleOrOwnLayers() {
		List<GeoLayer> ftList = new ArrayList<GeoLayer>();
		for (GeoLayer ft : layerService.getLayers()) {
			if (ft.isVisible(this) || ft.isLocal(this)) {
				ftList.add(ft);
			}
		}
		return ftList;
	}

	public GeoFeatureType getGeoFeatureType(String featureTypeName) {
		GeoFeatureType gft = getGeoFeatureTypeMap().get(featureTypeName);
		if (gft == null) {
			throw NotFoundExceptionType.NOTFOUND.createException();
		}
		return gft;
	}

	public GeoFeatureType createGeoFeatureType(String name, String description, GeoType geoType,
			int minZoom, int maxZoom, String style, int position) {
		GeoFeatureType gft = GeoFeatureType.create(this, name, description, geoType, minZoom, maxZoom, style, position);
		getGeoFeatureTypeMap().put(gft.getName(), gft);
		return gft;
	}
}
