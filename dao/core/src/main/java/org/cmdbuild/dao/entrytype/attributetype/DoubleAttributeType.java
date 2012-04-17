package org.cmdbuild.dao.entrytype.attributetype;



public class DoubleAttributeType implements CMAttributeType<Double> {

	public DoubleAttributeType() {
	}

	@Override
	public void accept(CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public Double convertNotNullValue(Object value) {
		Double doubleValue;
		if (value instanceof Double) {
			doubleValue = (Double) value;
		} else if (value instanceof String) {
			String stringValue = (String) value;
			if (stringValue.isEmpty()) {
				doubleValue = null;
			} else {
				// throws NumberFormatException
				doubleValue = Double.valueOf(stringValue);
			}
		} else {
			throw new IllegalArgumentException();
		}
		return doubleValue;
	}
}
