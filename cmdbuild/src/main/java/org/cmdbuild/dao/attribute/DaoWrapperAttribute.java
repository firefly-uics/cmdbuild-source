package org.cmdbuild.dao.attribute;

import java.util.Map;

import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.UndefinedAttributeType;
import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.exception.ORMException.ORMExceptionType;

public abstract class DaoWrapperAttribute extends AttributeImpl {

	protected static CMAttributeType<?> UNDEFINED_TYPE = new UndefinedAttributeType();

	protected CMAttributeType<?> daoType;

	protected DaoWrapperAttribute(BaseSchema schema, String name, Map<String, String> meta) {
		super(schema, name, meta);
		daoType = UNDEFINED_TYPE;
	}

	protected Object convertValue(Object value) {
		try {
			return daoType.convertValue(value);
		} catch (Throwable t) {
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		}
	}
}
