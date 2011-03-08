package org.cmdbuild.services.gis;

import org.cmdbuild.elements.interfaces.ICard;
import org.postgis.Geometry;

public class GeoFeature {

	final GeoFeatureType type;
	final ICard geoCard;

	public GeoFeature(ICard geoCard, GeoFeatureType type) {
		super();
		this.geoCard = geoCard;
		this.type = type;
	}

	public GeoFeatureType getType() {
		return type;
	}

	public Geometry getGeometry() {
		return (Geometry) geoCard.getValue(GeoFeatureType.GEOMETRY_ATTRIBUTE);
	}

	public ICard getMasterCard() {
		return (ICard) geoCard.getValue(GeoFeatureType.MASTER_ATTRIBUTE);
	}

	public void delete() {
		geoCard.delete();
	}

	public void setValue(String value) {
		geoCard.setValue(GeoFeatureType.GEOMETRY_ATTRIBUTE, value);
		geoCard.save();
	}
}
