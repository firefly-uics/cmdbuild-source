package org.cmdbuild.dao.attribute;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.exception.ORMException.ORMExceptionType;

public class DateAttribute extends AbstractDateAttribute {

	public static final String JSON_DATE_FORMAT="dd/MM/yy";
	public static final String SOAP_DATE_FORMAT="yyyy-MM-dd'T'HH:mm:ss";
	public static final String REST_DATE_FORMAT="yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	public static final String POSTGRES_DATE_FORMAT="yyyy-MM-dd";

	public DateAttribute(BaseSchema schema, String name, Map<String, String> meta) {
		super(schema, name, meta);
	}

	@Override
	public AttributeType getType() {
		return AttributeType.DATE;
	}

	@Override
	protected Object convertValue(Object value) {
		Date dateValue;
		if (value instanceof Date) {
			dateValue = normalizeToMidnight((Date) value);
		} else if (value instanceof String) {
			dateValue = convertDateString((String) value,
					JSON_DATE_FORMAT,
					SOAP_DATE_FORMAT,
					REST_DATE_FORMAT);
		} else {
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		}
		return dateValue;
	}

	/*
	 * If checking for midnight, instead of forcing it,
	 * don't forget about the timezone offset
	 */
	private Date normalizeToMidnight(final Date date) {
		final Calendar defaultCalendar = Calendar.getInstance();
		defaultCalendar.setTime(date);
		defaultCalendar.set(Calendar.HOUR_OF_DAY, 0);
		defaultCalendar.set(Calendar.MINUTE, 0);
		defaultCalendar.set(Calendar.SECOND, 0);
		defaultCalendar.set(Calendar.MILLISECOND, 0);
		return defaultCalendar.getTime();
	}
	
	@Override
	public String notNullValueToDBFormat(Object value) {
		return escapeAndQuote(new SimpleDateFormat(POSTGRES_DATE_FORMAT).format((Date)value));
	}

	@Override
	public String notNullValueToString(Object value) {
		return new SimpleDateFormat(JSON_DATE_FORMAT).format((Date)value);
	}
}
