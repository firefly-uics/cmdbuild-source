package org.cmdbuild.dao.entrytype.attributetype;


public class UndefinedAttributeType implements CMAttributeType<Object> {

	public UndefinedAttributeType() {
	}

	@Override
	public Object convertNotNullValue(Object value) {
		return value;
	}
}
