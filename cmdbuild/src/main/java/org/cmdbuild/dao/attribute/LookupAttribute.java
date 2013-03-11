package org.cmdbuild.dao.attribute;

import java.util.Map;

import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.exception.ORMException.ORMExceptionType;

public class LookupAttribute extends AttributeImpl {

	public LookupAttribute(final BaseSchema schema, final String name, final Map<String, String> meta) {
		super(schema, name, meta);
	}

	@Override
	public AttributeType getType() {
		return AttributeType.LOOKUP;
	}

	@Override
	protected Object convertValue(final Object value) {
		Lookup lookup;
		if (value instanceof Number) {
			final Integer intValue = ((Number) value).intValue();
			lookup = backend.getLookup(intValue);
			if (lookup == null && intValue > 0) {
				throw ORMExceptionType.ORM_TYPE_ERROR.createException();
			}
		} else if (value instanceof String) {
			final String stringValue = (String) value;
			lookup = getLookupFromString(stringValue);
			if (lookup == null && !stringValue.trim().isEmpty()) {
				throw ORMExceptionType.ORM_TYPE_ERROR.createException();
			}
		} else if (value instanceof Lookup) {
			lookup = (Lookup) value;
		} else {
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		}
		/*
		 * FIXME THIS CODE DOES NOT WORK if ((lookup != null) &&
		 * (!lookup.getType().equals(schema.getLookupType().getType()))) throw
		 * new ORMException(ORMException.ExceptionCode.ORM_TYPE_ERROR);
		 */
		return lookup;
	}

	private Lookup getLookupFromString(final String stringValue) {
		Lookup lookup;
		if (stringValue.isEmpty()) {
			lookup = null;
		} else {
			lookup = getLookupFromStringId(stringValue);
			if (lookup == null) {
				lookup = getLookupFromStringDescription(stringValue);
			}
		}
		return lookup;
	}

	private Lookup getLookupFromStringId(final String stringValue) {
		try {
			Lookup lookup = backend.getLookup(Integer.valueOf(stringValue));
			if ((lookup == null) || (!lookup.getType().equals(getLookupType().getType()))) {
				lookup = null;
			}
			return lookup;
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	private Lookup getLookupFromStringDescription(final String stringValue) {
		return backend.getLookup(getLookupType().getType(), stringValue);
	}

	@Override
	protected String notNullValueToDBFormat(final Object value) {
		return String.valueOf(((Lookup) value).getId());
	}
}
