package org.cmdbuild.dao.attribute;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.cmdbuild.common.Constants;
import org.cmdbuild.elements.interfaces.BaseSchema;

public class DateAttribute extends AbstractDateAttribute {

	public static final String POSTGRES_DATE_FORMAT = "yyyy-MM-dd";

	public DateAttribute(final BaseSchema schema, final String name, final Map<String, String> meta) {
		super(schema, name, meta);
	}

	@Override
	public AttributeType getType() {
		return AttributeType.DATE;
	}

	protected String getParsingPattern() {
		return Constants.DATE_PARSING_PATTERN;
	}

	protected Date normalizeDate(Date date) {
		return normalizeToMidnight(date);
	}

	/*
	 * If checking for midnight, instead of forcing it, don't forget about the
	 * timezone offset
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
	public String notNullValueToDBFormat(final Object value) {
		return escapeAndQuote(new SimpleDateFormat(POSTGRES_DATE_FORMAT).format((Date) value));
	}

	@Override
	public String notNullValueToString(final Object value) {
		return new SimpleDateFormat(Constants.DATE_PRINTING_PATTERN).format((Date) value);
	}
}
