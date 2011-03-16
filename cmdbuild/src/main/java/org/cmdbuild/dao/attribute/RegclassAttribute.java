package org.cmdbuild.dao.attribute;

import org.cmdbuild.elements.interfaces.BaseSchema;

public class RegclassAttribute extends IntegerAttribute {

	public RegclassAttribute(BaseSchema schema, String name) {
		super(schema, name);
	}

	@Override
	public AttributeType getType() {
		return AttributeType.REGCLASS;
	}
}
