package org.cmdbuild.dao.attribute;

import java.util.Map;

import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.exception.ORMException.ORMExceptionType;

public class DoubleAttribute extends DaoWrapperAttribute {

	private static CMAttributeType<?> DOUBLE_TYPE = new DoubleAttributeType();

	public DoubleAttribute(BaseSchema schema, String name, Map<String, String> meta) {
		super(schema, name, meta);
		daoType = DOUBLE_TYPE;
	}

	@Override
	public AttributeType getType() {
		return AttributeType.DOUBLE;
	}
}
