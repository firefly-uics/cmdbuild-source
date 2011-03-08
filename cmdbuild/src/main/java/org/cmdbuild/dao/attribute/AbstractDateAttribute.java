package org.cmdbuild.dao.attribute;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.exception.ORMException.ORMExceptionType;

public abstract class AbstractDateAttribute extends AttributeImpl {

	public AbstractDateAttribute(BaseSchema schema, String name) {
		super(schema, name);
	}

	protected Date convertDateString(String stringValue, String... formats) {
		if (stringValue.length() != 0) {
			for (String format : formats) {
				try {
					return new SimpleDateFormat(format).parse(stringValue);
				} catch (ParseException ex) {}
			}
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		} else {
			return null;
		}
	}
}
