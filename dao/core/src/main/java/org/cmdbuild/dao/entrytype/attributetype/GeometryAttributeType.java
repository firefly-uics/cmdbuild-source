package org.cmdbuild.dao.entrytype.attributetype;

public class GeometryAttributeType extends AbstractAttributeType<Object> {

	@Override
	public void accept(final CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected Object convertNotNullValue(final Object value) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

}
