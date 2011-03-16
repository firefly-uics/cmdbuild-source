package org.cmdbuild.dao.attribute;

import org.cmdbuild.dao.type.ByteArray;
import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.exception.ORMException.ORMExceptionType;

public class BinaryAttribute extends AttributeImpl {

	public BinaryAttribute(BaseSchema schema, String name) {
		super(schema, name);
	}

	@Override
	public AttributeType getType() {
		return AttributeType.BINARY;
	}

	@Override
	protected Object convertValue(Object value) {
		ByteArray byteArrayValue;
		if (value instanceof ByteArray) {
			byteArrayValue = (ByteArray) value;
		} else if (value instanceof byte[]) {
			byteArrayValue = ByteArray.valueOf((byte[])value);
		} else {
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		}
		return byteArrayValue;
	}
}
