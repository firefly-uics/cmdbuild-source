package org.cmdbuild.dao.entrytype.attributetype;

import java.util.Calendar;

import org.joda.time.DateTime;


public abstract class AbstractDateAttributeType extends AbstractAttributeType<DateTime> {

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

	protected final DateTime convertDateString(String stringValue) {
		// TODO
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
