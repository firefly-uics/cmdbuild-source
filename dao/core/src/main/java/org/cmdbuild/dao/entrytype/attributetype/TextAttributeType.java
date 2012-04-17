package org.cmdbuild.dao.entrytype.attributetype;


public class TextAttributeType implements CMAttributeType<String> {

	public TextAttributeType() {
	}

	@Override
	public void accept(CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String convertNotNullValue(Object value) {
		if (!(value instanceof String)) {
			throw new IllegalArgumentException();
		}
		final String stringValue = (String) value;
		if (stringLimitExceeded(stringValue)) {
			throw new IllegalArgumentException();
		}
		if (stringValue.isEmpty()) {
			return null;
		} else {
			return stringValue;
		}
	}

	protected boolean stringLimitExceeded(final String stringValue) {
		return false;
	}
}
