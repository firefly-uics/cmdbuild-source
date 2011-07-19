package org.cmdbuild.dao.attribute;

import java.util.Map;

import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.elements.interfaces.BaseSchema;

public class DecimalAttribute extends DaoWrapperAttribute {

	public DecimalAttribute(BaseSchema schema, String name, Map<String, String> meta) {
		super(schema, name, meta);
		daoType = new DecimalAttributeType(getPrecision(), getScale());
	}

	@Override
	public AttributeType getType() {
		return AttributeType.DECIMAL;
	}
}
