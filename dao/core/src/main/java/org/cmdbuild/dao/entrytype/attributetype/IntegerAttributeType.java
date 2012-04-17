package org.cmdbuild.dao.entrytype.attributetype;



public class IntegerAttributeType implements CMAttributeType<Integer> {

	public IntegerAttributeType() {
	}

	@Override
	public void accept(CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public Integer convertNotNullValue(Object value) {
		Integer intValue;
		if (value instanceof Integer) {
			intValue = (Integer) value;
		} else if (value instanceof Number) {
			intValue = ((Number) value).intValue();
		} else if (value instanceof String) {
			String stringValue = (String) value;
			if (stringValue.isEmpty()) {
				intValue = null;
			} else {
				// throws NumberFormatException
				intValue = Integer.valueOf(stringValue);
			}
		} else {
			throw new IllegalArgumentException();
		}
		return intValue;
	}
}
