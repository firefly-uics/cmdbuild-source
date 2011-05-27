package org.cmdbuild.dao.attribute;

import java.sql.SQLException;

import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.postgis.Geometry;
import org.postgis.PGgeometry;

public class GeometryAttribute extends AttributeImpl {

	private AttributeType subType;

	public GeometryAttribute(BaseSchema schema, String name, AttributeType subType) {
		super(schema, name);
		this.subType = subType;
	}

	public AttributeType getType() {
		return subType;
	}

	@Override
	protected Object convertValue(Object value) {
		Geometry geometryValue;
		if (value instanceof String) {
			String stringValue = (String) value;
			if (stringValue.isEmpty()) {
				geometryValue = null;
			} else {
				try {
					geometryValue = PGgeometry.geomFromString(stringValue);
				} catch (SQLException e) {
					throw ORMExceptionType.ORM_TYPE_ERROR.createException();
				}
			}
		} else {
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		}
		return geometryValue;
	}

	@Override
	public String notNullValueToDBFormat(Object value) {
		return String.format("ST_GeomFromText('%s',900913)", value.toString());
	}
}
