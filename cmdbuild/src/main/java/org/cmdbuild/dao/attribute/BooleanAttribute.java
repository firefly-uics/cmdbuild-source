package org.cmdbuild.dao.attribute;

import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.exception.ORMException.ORMExceptionType;

public class BooleanAttribute extends AttributeImpl {

	public BooleanAttribute(BaseSchema schema, String name) {
		super(schema, name);
	}

	@Override
	public AttributeType getType() {
		return AttributeType.BOOLEAN;
	}

	@Override
	protected Object convertValue(Object value) {
		Boolean booleanValue;
		if (value instanceof Boolean) {
			booleanValue = (Boolean) value;
		} else if (value instanceof String) {
			String stringValue = (String) value;
			if (stringValue.isEmpty()) {
				booleanValue = null;
			} else {
				booleanValue = Boolean.valueOf(stringValue);
			}
		} else {
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		}
		return booleanValue;
	}
}
