package org.cmdbuild.dao.entrytype.attributetype;

import org.apache.commons.lang.Validate;


public class StringAttributeType implements CMAttributeType {

	public final int length;

	public StringAttributeType(final Integer length) {
		Validate.isTrue(length > 0);
		this.length = length;
	}
}
