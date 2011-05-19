package org.cmdbuild.dao.attribute;

import org.cmdbuild.elements.interfaces.BaseSchema;

public class CharAttribute extends TextAttribute {

	public CharAttribute(BaseSchema schema, String name) {
		super(schema, name);
	}

	@Override
	public AttributeType getType() {
		return AttributeType.CHAR;
	}

	@Override
	protected boolean stringLimitExceeded(String stringValue) {
		return (stringValue.length() > 1);
	}
}
