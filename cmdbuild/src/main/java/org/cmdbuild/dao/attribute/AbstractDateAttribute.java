package org.cmdbuild.dao.attribute;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.cmdbuild.common.Constants;
import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.joda.time.DateTime;

public abstract class AbstractDateAttribute extends AttributeImpl {

	public AbstractDateAttribute(final BaseSchema schema, final String name, final Map<String, String> meta) {
		super(schema, name, meta);
	}

	@Override
	protected Object convertValue(final Object value) {
		final Date dateValue;
		if (value instanceof Date) {
			dateValue = normalizeDate((Date) value);
		} else if (value instanceof DateTime) {
			long millis = ((DateTime) value).getMillis();
			dateValue = normalizeDate(new Date(millis));
		} else if (value instanceof Calendar) {
			dateValue = normalizeDate(((Calendar) value).getTime());
		} else if (value instanceof String) {
			dateValue = convertDateString((String) value, getParsingPattern(),
					Constants.SOAP_ALL_DATES_PARSING_PATTERN);
		} else {
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		}
		return dateValue;
	}

	protected Date normalizeDate(Date date) {
		return date;
	}

	protected abstract String getParsingPattern();

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
