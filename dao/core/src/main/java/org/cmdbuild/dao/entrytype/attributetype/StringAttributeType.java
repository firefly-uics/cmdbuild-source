package org.cmdbuild.dao.entrytype.attributetype;

import org.apache.commons.lang.Validate;


public class StringAttributeType extends TextAttributeType {

	public final int length;

	public StringAttributeType(final Integer length) {
		Validate.isTrue(length > 0);
		this.length = length;
	}

	@Override
	public void accept(CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected boolean stringLimitExceeded(String stringValue) {
		return (stringValue.length() > length);
	}
}
