package org.cmdbuild.dao.entrytype.attributetype;


public class ForeignKeyAttributeType extends AbstractAttributeType<Object> {

	public ForeignKeyAttributeType() {
	}

	@Override
	public void accept(CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected Object convertNotNullValue(Object value) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
