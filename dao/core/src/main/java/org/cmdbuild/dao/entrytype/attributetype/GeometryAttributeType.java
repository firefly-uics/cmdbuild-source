package org.cmdbuild.dao.entrytype.attributetype;


public class GeometryAttributeType implements CMAttributeType<Object> {

	public GeometryAttributeType() {
		// Geometry type needs to be provided
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
