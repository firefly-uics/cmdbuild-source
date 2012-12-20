package org.cmdbuild.dao.attribute;

import java.util.Map;

import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.elements.interfaces.BaseSchema;

public class IntegerAttribute extends DaoWrapperAttribute {

	static CMAttributeType<Integer> INTEGER_TYPE = new IntegerAttributeType();

	public IntegerAttribute(BaseSchema schema, String name, Map<String, String> meta) {
		super(schema, name, meta);
		daoType = INTEGER_TYPE;
	}

	public AttributeType getType() {
		return AttributeType.INTEGER;
	}
}
