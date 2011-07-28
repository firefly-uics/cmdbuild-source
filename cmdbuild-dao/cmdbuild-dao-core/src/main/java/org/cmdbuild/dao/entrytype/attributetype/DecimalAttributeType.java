package org.cmdbuild.dao.entrytype.attributetype;

import java.math.BigDecimal;

import org.apache.commons.lang.Validate;


public class DecimalAttributeType implements CMAttributeType<BigDecimal> {

	public final int precision;
	public final int scale;

	public DecimalAttributeType(final Integer precision, final Integer scale) {
		Validate.isTrue(precision > 0);
		Validate.isTrue(scale >= 0 && precision >= scale);
		this.precision = precision;
		this.scale = scale;
	}

	@Override
	public BigDecimal convertNotNullValue(Object value) {
		BigDecimal decimalValue;
		if (value instanceof BigDecimal) {
			decimalValue = (BigDecimal) value;
		} else if (value instanceof String) {
			String stringValue = (String) value;
			if (stringValue.isEmpty()) {
				decimalValue = null;
			} else {
				// throws NumberFormatException
				decimalValue = new BigDecimal(stringValue);
			}
		} else if (value instanceof Double) {
			decimalValue = new BigDecimal((Double) value);
		} else {
			throw new IllegalArgumentException();
		}
		return decimalValue;
	}
}
