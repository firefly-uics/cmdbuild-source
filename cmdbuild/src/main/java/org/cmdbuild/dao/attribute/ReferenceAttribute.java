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
			try {
				final int intValue = Integer.parseInt(stringValue);
				referenceValue = createReference(intValue);
			} catch (NumberFormatException e) {
				referenceValue = null;
			}
		} else if (value instanceof Number) {
			final int intValue = ((Number) value).intValue();
			referenceValue = createReference(intValue);
		} else {
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		}
		return referenceValue;
	}

	private Reference createReference(final int intValue) {
		if (intValue <= 0) {
			return null;
		}
		return new Reference(getReferenceDirectedDomain(), intValue, null);
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
