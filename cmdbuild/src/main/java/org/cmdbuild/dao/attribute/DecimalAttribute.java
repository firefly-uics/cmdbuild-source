package org.cmdbuild.dao.attribute;

import java.math.BigDecimal;

import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.exception.ORMException.ORMExceptionType;

public class DecimalAttribute extends AttributeImpl {

	public DecimalAttribute(BaseSchema schema, String name) {
		super(schema, name);
	}

	@Override
	public AttributeType getType() {
		return AttributeType.DECIMAL;
	}

	@Override
	protected Object convertValue(Object value) {
		BigDecimal decimalValue;
		if (value instanceof BigDecimal) {
			decimalValue = (BigDecimal) value;
		} else if (value instanceof String) {
			String stringValue = (String) value;
			if (stringValue.isEmpty()) {
				decimalValue = null;
			} else {
				try {
					decimalValue = new BigDecimal(stringValue);
				} catch (NumberFormatException e) {
					throw ORMExceptionType.ORM_TYPE_ERROR.createException();
				}
			}
		} else if (value instanceof Double) {
			decimalValue = new BigDecimal((Double) value);
		} else {
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		}
		return decimalValue;
	}
}
