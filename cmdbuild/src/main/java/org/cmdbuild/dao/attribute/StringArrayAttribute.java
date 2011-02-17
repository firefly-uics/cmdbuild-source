package org.cmdbuild.dao.attribute;

import org.cmdbuild.dao.type.StringArray;
import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.exception.ORMException.ORMExceptionType;

public class StringArrayAttribute extends AttributeImpl {

	public StringArrayAttribute(BaseSchema schema, String name) {
		super(schema, name);
	}

	@Override
	public AttributeType getType() {
		return AttributeType.STRINGARRAY;
	}

	@Override
	protected Object convertValue(Object value) {
		StringArray stringArray;
		if (value instanceof StringArray) {
			stringArray = (StringArray) value;
		} else if (value instanceof String[]) {
			stringArray = StringArray.valueOf((String[])value);
		} else if (value instanceof String) {
			stringArray = StringArray.valueOf((String)value);
		} else {
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		}
		return stringArray;
	}
}
