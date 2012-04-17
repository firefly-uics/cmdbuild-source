package org.cmdbuild.dao.entrytype.attributetype;

import org.joda.time.DateTime;


public abstract class AbstractDateAttributeType implements CMAttributeType<DateTime> {

	public DateTime convertNotNullValue(Object value) {
		if (value instanceof String) {
			return convertDateString((String) value);
		} else if (value instanceof java.util.Date) {
			final long instant = ((java.util.Date) value).getTime();
			return new DateTime(instant);
		} else if (value instanceof DateTime) {
			return (DateTime) value;
		} else {
			throw new IllegalArgumentException();
		}
	}

	protected final DateTime convertDateString(String stringValue) {
		// TODO
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
