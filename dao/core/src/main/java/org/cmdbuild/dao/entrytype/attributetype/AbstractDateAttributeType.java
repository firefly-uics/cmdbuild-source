package org.cmdbuild.dao.entrytype.attributetype;

import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;


public abstract class AbstractDateAttributeType extends AbstractAttributeType<DateTime> {

	public static final String SOAP_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	@Override
	protected DateTime convertNotNullValue(Object value) {
		if (value instanceof String) {
			return convertDateString((String) value);
		} else if (value instanceof java.util.Date) {
			final long instant = ((java.util.Date) value).getTime();
			return new DateTime(instant);
		} else if (value instanceof DateTime) {
			return (DateTime) value;
		} else if (value instanceof Calendar) {
			final long instant = ((Calendar) value).getTimeInMillis();
			return new DateTime(instant);
		} else {
			throw new IllegalArgumentException();
		}
	}

	protected final DateTime convertDateString(final String stringValue) {

		if (StringUtils.EMPTY.equals(stringValue)) {
			return null;
		}

		for (final DateTimeFormatter formatter : getFormatters()) {
			try {
				return formatter.parseDateTime(stringValue);
			} catch (IllegalArgumentException e) {
				// try the next one
			}
		}

		return null;
	}

	abstract protected DateTimeFormatter[] getFormatters();
}
