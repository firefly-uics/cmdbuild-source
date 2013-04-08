package org.cmdbuild.dao.attribute;

import java.util.Map;

import org.cmdbuild.dao.type.IntArray;
import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.exception.ORMException.ORMExceptionType;

public class IntArrayAttribute extends AttributeImpl {

	public IntArrayAttribute(BaseSchema schema, String name, Map<String, String> meta) {
		super(schema, name, meta);
	}

	@Override
	public AttributeType getType() {
		return AttributeType.INTARRAY;
	}

	@Override
	protected Object convertValue(Object value) {
		IntArray intArray;
		if (value instanceof IntArray) {
			intArray = (IntArray) value;
		} else if (value instanceof Integer[]) {
			intArray = IntArray.valueOf((Integer[])value);
		} else if (value instanceof String) {
			intArray = IntArray.valueOf((String)value);
		} else {
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		}
		return intArray;
	}
}
