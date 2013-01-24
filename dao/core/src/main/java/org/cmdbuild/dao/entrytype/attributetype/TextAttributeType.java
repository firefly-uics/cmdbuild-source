package org.cmdbuild.dao.entrytype.attributetype;

public class TextAttributeType extends AbstractAttributeType<String> {

	public TextAttributeType() {
	}

	@Override
	public void accept(final CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected String convertNotNullValue(final Object value) {
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
