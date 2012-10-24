package org.cmdbuild.dao.entrytype.attributetype;

public class UndefinedAttributeType implements CMAttributeType<Object> {

	public UndefinedAttributeType() {
	}

	@Override
	public void accept(final CMAttributeTypeVisitor visitor) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final Object convertValue(final Object value) {
		return value;
	}
}
