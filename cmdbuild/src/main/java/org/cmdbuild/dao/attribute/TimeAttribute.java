package org.cmdbuild.dao.attribute;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.exception.ORMException.ORMExceptionType;

public class TimeAttribute extends AbstractDateAttribute {

	public static final String JSON_DATETIME_FORMAT="HH:mm:ss";
	public static final String SOAP_DATETIME_FORMAT="yyyy-MM-dd'T'HH:mm:ss";
	public static final String REST_DATETIME_FORMAT="yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	public static final String POSTGRES_DATETIME_FORMAT="HH:mm:ss";

	public TimeAttribute(BaseSchema schema, String name, Map<String, String> meta) {
		super(schema, name, meta);
	}

	@Override
	public AttributeType getType() {
		return AttributeType.TIME;
	}

	@Override
	protected Object convertValue(Object value) {
		Date dateValue;
		if (value instanceof Date) {
			dateValue = (Date) value;
		} else if (value instanceof String) {
			dateValue = convertDateString((String) value,
					JSON_DATETIME_FORMAT,
					SOAP_DATETIME_FORMAT,
					REST_DATETIME_FORMAT);
		} else {
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		}
		return dateValue;
	}

	@Override
	public String notNullValueToDBFormat(Object value) {
		return escapeAndQuote(new SimpleDateFormat(POSTGRES_DATETIME_FORMAT).format((Date)value));
	}

	@Override
	public String notNullValueToString(Object value) {
		return new SimpleDateFormat(JSON_DATETIME_FORMAT).format((Date)value);
	}
}
