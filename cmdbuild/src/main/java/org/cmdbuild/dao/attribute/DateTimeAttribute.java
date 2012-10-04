package org.cmdbuild.dao.attribute;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.cmdbuild.common.Constants;
import org.cmdbuild.elements.interfaces.BaseSchema;

public class DateTimeAttribute extends AbstractDateAttribute {

	public static final String POSTGRES_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public DateTimeAttribute(final BaseSchema schema, final String name, final Map<String, String> meta) {
		super(schema, name, meta);
	}

	@Override
	public AttributeType getType() {
		return AttributeType.TIMESTAMP;
	}

	protected String getParsingPattern() {
		return Constants.DATETIME_PARSING_PATTERN;
	}

	@Override
	public String notNullValueToDBFormat(final Object value) {
		return escapeAndQuote(new SimpleDateFormat(POSTGRES_DATETIME_FORMAT).format((Date) value));
	}

	@Override
	public String notNullValueToString(final Object value) {
		return new SimpleDateFormat(Constants.DATETIME_PRINTING_PATTERN).format((Date) value);
	}
}
