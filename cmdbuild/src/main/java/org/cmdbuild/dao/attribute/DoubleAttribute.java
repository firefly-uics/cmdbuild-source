package org.cmdbuild.dao.attribute;

import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.exception.ORMException.ORMExceptionType;

public class DoubleAttribute extends AttributeImpl {

	public DoubleAttribute(BaseSchema schema, String name) {
		super(schema, name);
	}

	@Override
	public AttributeType getType() {
		return AttributeType.DOUBLE;
	}

	@Override
	protected Object convertValue(Object value) {
		Double doubleValue;
		if (value instanceof Double) {
			doubleValue = (Double) value;
		} else if (value instanceof String) {
			String stringValue = (String) value;
			if (stringValue.isEmpty()) {
				doubleValue = null;
			} else {
				try {
					doubleValue = Double.valueOf(stringValue);
				} catch (NumberFormatException e) {
					throw ORMExceptionType.ORM_TYPE_ERROR.createException();
				}
			}
		} else {
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		}
		return doubleValue;
	}
}
