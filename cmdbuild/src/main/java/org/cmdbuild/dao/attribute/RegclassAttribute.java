package org.cmdbuild.dao.attribute;

import java.util.Map;

import org.cmdbuild.elements.interfaces.BaseSchema;

public class RegclassAttribute extends IntegerAttribute {

	public RegclassAttribute(BaseSchema schema, String name, Map<String, String> meta) {
		super(schema, name, meta);
	}

	@Override
	public AttributeType getType() {
		return AttributeType.REGCLASS;
	}
}
