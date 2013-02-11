package org.cmdbuild.services.gis;

import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.model.gis.LayerMetadata;


public class GeoFeatureLayer extends LayerMetadata {

	static final String MASTER_ATTRIBUTE = "Master";
	static final String GEOMETRY_ATTRIBUTE = "Geometry";

	public enum GeoType {
		POINT(AttributeType.POINT),
		LINESTRING(AttributeType.LINESTRING),
		POLYGON(AttributeType.POLYGON);

		private final AttributeType attributeType;

		private GeoType(AttributeType attributeType) {
			this.attributeType = attributeType;
		}

		public static GeoType valueOf(AttributeType attributeType) {
			for (GeoType gt : GeoType.values()) {
				if (gt.attributeType.equals(attributeType)) {
					return gt;
				}
			}
			throw new IllegalArgumentException();
		}

		public AttributeType getAttributeType() {
			return attributeType;
		}
	}
}