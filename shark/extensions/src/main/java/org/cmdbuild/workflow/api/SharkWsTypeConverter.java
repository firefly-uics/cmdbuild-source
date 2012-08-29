package org.cmdbuild.workflow.api;

import static org.apache.commons.lang.StringUtils.EMPTY;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.cmdbuild.api.fluent.ws.WsFluentApiExecutor.WsType;
import org.cmdbuild.common.Constants;

public abstract class SharkWsTypeConverter {

	protected String toWsType(final WsType wsType, final Object value) {
		final String stringValue;
		if (value == null) {
			stringValue = EMPTY;
		} else if (value instanceof Number) {
			stringValue = Number.class.cast(value).toString();
		} else if (value instanceof Date) {
			final SimpleDateFormat formatter = new SimpleDateFormat(Constants.SOAP_ALL_DATES_PRINTING_PATTERN);
			final Date date = Date.class.cast(value);
			stringValue = formatter.format(date);
		} else {
			stringValue = value.toString();
		}
		return stringValue;
	}

	protected String toClientType(final WsType wsType, final String attributeName, final String wsValue) {
		return wsValue;
	}

}