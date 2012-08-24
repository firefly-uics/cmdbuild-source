package org.cmdbuild.dao.attribute;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.cmdbuild.dao.entrytype.attributetype.AbstractDateAttributeType;
import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.exception.ORMException.ORMExceptionType;

public abstract class AbstractDateAttribute extends AttributeImpl {

	public static final String SOAP_DATETIME_FORMAT = AbstractDateAttributeType.SOAP_DATETIME_FORMAT;

	public AbstractDateAttribute(final BaseSchema schema, final String name, final Map<String, String> meta) {
		super(schema, name, meta);
	}

	protected Date convertDateString(final String stringValue, final String... formats) {
		if (stringValue.length() != 0) {
			for (final String format : formats) {
				try {
					return new SimpleDateFormat(format).parse(stringValue);
				} catch (final ParseException ex) {
				}
			}
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		} else {
			return null;
		}
	}
}
