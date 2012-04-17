package org.cmdbuild.dao.entrytype.attributetype;


public class ForeignKeyAttributeType implements CMAttributeType<Object> {

	public ForeignKeyAttributeType() {
	}

	@Override
	public void accept(CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public Object convertNotNullValue(Object value) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
