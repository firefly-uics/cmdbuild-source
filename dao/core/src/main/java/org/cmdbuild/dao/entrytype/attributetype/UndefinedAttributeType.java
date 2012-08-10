package org.cmdbuild.dao.entrytype.attributetype;


public class UndefinedAttributeType implements CMAttributeType<Object> {

	public UndefinedAttributeType() {
	}

	@Override
	public void accept(CMAttributeTypeVisitor visitor) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final Object convertValue(Object value) {
		return value;
	}
}
