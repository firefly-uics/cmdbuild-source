package org.cmdbuild.dao.attribute;

import java.util.Map;

import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.Reference;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.exception.ORMException.ORMExceptionType;

public class ReferenceAttribute extends AttributeImpl {

	public ReferenceAttribute(BaseSchema schema, String name, Map<String, String> meta) {
		super(schema, name, meta);
	}

	@Override
	public AttributeType getType() {
		return AttributeType.REFERENCE;
	}

	@Override
	protected Object convertValue(Object value) {
		Reference referenceValue;
		if (value instanceof Reference) {
			referenceValue = (Reference) value;
		} else if (value instanceof String) {
			String stringValue = (String) value;
			if (stringValue.isEmpty()) {
				referenceValue = null;
			} else {
				referenceValue = new Reference(getReferenceDirectedDomain(), Integer.parseInt(stringValue), null);
			}
		} else {
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		}
		return referenceValue;
	}

	@Override
	protected String notNullValueToString(Object value) {
		return ((Reference)value).getDescription();
	}

	@Override
	protected String notNullValueToDBFormat(Object value) {
		return String.valueOf(((Reference)value).getId());
	}

}
