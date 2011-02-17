package org.cmdbuild.services.gis;

import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.proxy.CardForwarder;
import org.cmdbuild.exception.NotFoundException;

public class GeoCard extends CardForwarder {

	private final GeoTable geoTable;

	public GeoCard(ICard card) {
		super(card);
		geoTable = new GeoTable(get().getSchema());
	}

	public void setGeoFeatureValue(String name, String value) {
		final GeoFeatureType ft = geoTable.getGeoFeatureType(name);
		try {
			GeoFeature featureCard = ft.query().master(this).get();
			if (value != null && !value.trim().isEmpty()) {
				featureCard.setValue(value);
			} else {
				featureCard.delete();
			}
		} catch (NotFoundException e) {
			if (value != null && !value.trim().isEmpty()) {
				ft.create(this, value);
			}
		}
	}
}
