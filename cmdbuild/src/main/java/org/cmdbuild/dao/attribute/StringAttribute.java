package org.cmdbuild.dao.attribute;

import java.util.Map;

import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.elements.interfaces.BaseSchema;

public class StringAttribute extends TextAttribute {

	public StringAttribute(BaseSchema schema, String name, Map<String, String> meta) {
		super(schema, name, meta);
		daoType = new StringAttributeType(this.getLength());
	}

	@Override
	public AttributeType getType() {
		return AttributeType.STRING;
	}
}
