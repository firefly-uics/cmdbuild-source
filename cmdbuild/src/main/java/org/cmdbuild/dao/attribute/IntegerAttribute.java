package org.cmdbuild.dao.attribute;

import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.exception.ORMException.ORMExceptionType;

public class IntegerAttribute extends AttributeImpl {

	public IntegerAttribute(BaseSchema schema, String name) {
		super(schema, name);
	}

	public AttributeType getType() {
		return AttributeType.INTEGER;
	}

	@Override
	protected Object convertValue(Object value) {
		return convertValueToInteger(value);
	}

	protected final Integer convertValueToInteger(Object value) {
		Integer intValue;
		if (value instanceof Integer) {
			intValue = (Integer) value;
		} else if (value instanceof String) {
			String stringValue = (String) value;
			if (stringValue.isEmpty()) {
				intValue = null;
			} else {
				intValue = Integer.valueOf(stringValue);
			}
		} else {
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		}
		return intValue;
	}
}
