package org.cmdbuild.dao.entrytype.attributetype;


public class BooleanAttributeType implements CMAttributeType<Boolean> {

	public BooleanAttributeType() {
	}

	@Override
	public void accept(CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public Boolean convertNotNullValue(Object value) {
		final Boolean booleanValue;
		if (value instanceof Boolean) {
			booleanValue = (Boolean) value;
		} else if (value instanceof String) {
			String stringValue = (String) value;
			if (stringValue.isEmpty()) {
				booleanValue = null;
			} else {
				booleanValue = Boolean.valueOf(stringValue);
			}
		} else {
			throw new IllegalArgumentException();
		}
		return booleanValue;
	}
}
