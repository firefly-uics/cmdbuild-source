package org.cmdbuild.dao.attribute;

import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.services.SchemaCache;

public class LookupAttribute extends AttributeImpl {

	public LookupAttribute(BaseSchema schema, String name) {
		super(schema, name);
	}

	@Override
	public AttributeType getType() {
		return AttributeType.LOOKUP;
	}

	@Override
	protected Object convertValue(Object value) {
		Lookup lookup;
		if (value instanceof Integer) {
			lookup = SchemaCache.getInstance().getLookup((Integer) value);
		} else if (value instanceof String) {
			lookup = getLookupFromString((String) value);
		} else if (value instanceof Lookup) {
			lookup = (Lookup) value;
		} else {
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		}
		/* FIXME THIS CODE DOES NOT WORK
		if ((lookup != null)
				&& (!lookup.getType().equals(schema.getLookupType().getType())))
			throw new ORMException(ORMException.ExceptionCode.ORM_TYPE_ERROR);
		*/
		return lookup;
	}

	private Lookup getLookupFromString(String stringValue) {
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

	private Lookup getLookupFromStringId(String stringValue) {
		Lookup lookup;
		try {
			lookup = SchemaCache.getInstance().getLookup(Integer.valueOf(stringValue));
			if (!lookup.getType().equals(getLookupType().getType())) {
				lookup = null;
			}
		} catch (NumberFormatException e) {
			lookup = null;
		}
		return lookup;
	}

	private Lookup getLookupFromStringDescription(String stringValue) {
		return SchemaCache.getInstance().getLookup(getLookupType().getType(), stringValue);
	}

	@Override
	protected String notNullValueToDBFormat(Object value) {
		return String.valueOf(((Lookup)value).getId());
	}
}
