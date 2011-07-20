package org.cmdbuild.dao.entrytype.attributetype;


public class TextAttributeType implements CMAttributeType<String> {

	public TextAttributeType() {
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
		return stringValue;
	}

	protected boolean stringLimitExceeded(final String stringValue) {
		return false;
	}
}
