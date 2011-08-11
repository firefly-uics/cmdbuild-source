package org.cmdbuild.dao.entrytype.attributetype;


public class ReferenceAttributeType implements CMAttributeType<Object> {

	public ReferenceAttributeType() {
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
