package org.cmdbuild.services.gis;

import org.cmdbuild.elements.interfaces.ICard;
import org.postgis.Geometry;

public class GeoFeature {

	final ICard geoCard;

	public GeoFeature(ICard geoCard) {
		super();
		this.geoCard = geoCard;
	}

	public Geometry getGeometry() {
		return (Geometry) geoCard.getValue(GeoFeatureLayer.GEOMETRY_ATTRIBUTE);
	}

	public ICard getMasterCard() {
		return (ICard) geoCard.getValue(GeoFeatureLayer.MASTER_ATTRIBUTE);
	}

	public void delete() {
		geoCard.delete();
	}

	public void setValue(String value) {
		geoCard.setValue(GeoFeatureLayer.GEOMETRY_ATTRIBUTE, value);
		geoCard.save();
	}
}
