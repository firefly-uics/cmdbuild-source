package org.cmdbuild.dao.attribute;

import org.cmdbuild.elements.interfaces.BaseSchema;

public class StringAttribute extends TextAttribute {

	public StringAttribute(BaseSchema schema, String name) {
		super(schema, name);
	}

	@Override
	public AttributeType getType() {
		return AttributeType.STRING;
	}

	@Override
	protected boolean stringLimitExceeded(String stringValue) {
		return (stringValue.length() > getStringSizeLimit());
	}

	private int getStringSizeLimit() {
		// TODO check for the correct length when metadata are implemented
		return Integer.MAX_VALUE;
	}
}
