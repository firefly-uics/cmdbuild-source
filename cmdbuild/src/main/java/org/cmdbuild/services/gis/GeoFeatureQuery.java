package org.cmdbuild.services.gis;

import java.util.Iterator;

import org.cmdbuild.elements.filters.BBoxFilter;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ICard.CardAttributes;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;

public class GeoFeatureQuery implements Iterable<GeoFeature> {

	private GeoFeatureType geoFeatureType;
	private String bbox;
	private Integer masterId;
	private ITable onlyFrom;

	public GeoFeatureQuery(GeoFeatureType geoFeatureType) {
		this.geoFeatureType = geoFeatureType;
	}

	public GeoFeatureQuery bbox(String bbox) {
		this.bbox = bbox;
		return this;
	}

	public GeoFeatureQuery master(ICard masterCard) {
		this.masterId = masterCard.getId();
		return this;
	}

	public GeoFeatureQuery onlyFrom(ITable table) {
		if (!geoFeatureType.getMasterTable().equals(table)) {
			onlyFrom = table;
		}
		return this;
	}

	private class FeatureQueryIterator implements Iterator<GeoFeature> {

		private Iterator<ICard> geoCardIterator;

		private FeatureQueryIterator(CardQuery geoCardQuery) {
			this.geoCardIterator = geoCardQuery.iterator();
		}

		@Override
		public boolean hasNext() {
			return geoCardIterator.hasNext();
		}

		@Override
		public GeoFeature next() {
			ICard geoCard = geoCardIterator.next();
			return new GeoFeature(geoCard, GeoFeatureQuery.this.geoFeatureType);
		}

		@Override
		public void remove() {
			geoCardIterator.remove();
		}
	}

	@Override
	public Iterator<GeoFeature> iterator() {
		CardQuery geoCardQuery = geoFeatureType.getGeoAttributeTable().cards().list().ignoreStatus();
		if (bbox != null) {
			geoCardQuery.filter(new BBoxFilter(geoFeatureType.getGeoAttributeTable().getAttribute(GeoFeatureType.GEOMETRY_ATTRIBUTE), bbox));
		}
		if (masterId != null) {
			geoCardQuery.filter(GeoFeatureType.MASTER_ATTRIBUTE, AttributeFilterType.EQUALS, String.valueOf(masterId));
		}
		if (onlyFrom != null) {
			final CardQuery q = onlyFrom.cards().list().attributes(CardAttributes.Id.toString());
			geoCardQuery.filter(GeoFeatureType.MASTER_ATTRIBUTE, q);
		}
		return new FeatureQueryIterator(geoCardQuery);
	}

	public GeoFeature get() {
		Iterator<GeoFeature> i = iterator();
		if (!i.hasNext()) {
			throw NotFoundExceptionType.CARD_NOTFOUND.createException(geoFeatureType.getName());
		}
		return i.next();
	}
}
