package org.cmdbuild.dao.attribute;

import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.exception.ORMException.ORMExceptionType;

public class TextAttribute extends AttributeImpl {

	public TextAttribute(BaseSchema schema, String name) {
		super(schema, name);
	}

	@Override
	public AttributeType getType() {
		return AttributeType.TEXT;
	}

	@Override
	protected Object convertValue(Object value) {
		if (!(value instanceof String)) {
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		}
		String stringValue = (String) value;
		if (stringLimitExceeded(stringValue)) {
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		}
		if (stringValue.isEmpty()) {
			return null;
		} else {
			return stringValue;
		}
	}

	protected boolean stringLimitExceeded(String stringValue) {
		return false;
	}

	@Override
	public String notNullValueToDBFormat(Object value) {
		return escapeAndQuote((String)value);
	}
}
