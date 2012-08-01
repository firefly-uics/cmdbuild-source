package org.cmdbuild.dao.attribute;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.exception.ORMException.ORMExceptionType;

public class DateTimeAttribute extends AbstractDateAttribute {

	public static final String JSON_DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
	public static final String LEGACY_JSON_DATETIME_FORMAT = "dd/MM/yy HH:mm:ss";
	public static final String POSTGRES_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public DateTimeAttribute(final BaseSchema schema, final String name, final Map<String, String> meta) {
		super(schema, name, meta);
	}

	@Override
	public AttributeType getType() {
		return AttributeType.TIMESTAMP;
	}

	@Override
	protected Object convertValue(final Object value) {
		Date dateValue;
		if (value instanceof Date) {
			dateValue = (Date) value;
		} else if (value instanceof Calendar) {
			dateValue = ((Calendar) value).getTime();
		} else if (value instanceof String) {
			dateValue = convertDateString((String) value, JSON_DATETIME_FORMAT, DateAttribute.JSON_DATE_FORMAT,
					SOAP_DATETIME_FORMAT, REST_DATETIME_FORMAT, LEGACY_JSON_DATETIME_FORMAT);
		} else {
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		}
		return dateValue;
	}

	@Override
	public String notNullValueToDBFormat(final Object value) {
		return escapeAndQuote(new SimpleDateFormat(POSTGRES_DATETIME_FORMAT).format((Date) value));
	}

	@Override
	public String notNullValueToString(final Object value) {
		return new SimpleDateFormat(JSON_DATETIME_FORMAT).format((Date) value);
	}
}
