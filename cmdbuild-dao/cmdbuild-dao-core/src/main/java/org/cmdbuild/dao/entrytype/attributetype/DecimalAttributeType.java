package org.cmdbuild.dao.entrytype.attributetype;

import org.apache.commons.lang.Validate;


public class DecimalAttributeType implements CMAttributeType {

	public final int precision;
	public final int scale;

	public DecimalAttributeType(final Integer precision, final Integer scale) {
		Validate.isTrue(precision > 0);
		Validate.isTrue(scale >= 0 && precision >= scale);
		this.precision = precision;
		this.scale = scale;
	}
}
