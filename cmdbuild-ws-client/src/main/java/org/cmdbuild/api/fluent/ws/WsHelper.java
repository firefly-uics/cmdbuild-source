package org.cmdbuild.api.fluent.ws;

import static org.apache.commons.lang.StringUtils.EMPTY;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.cmdbuild.services.soap.Private;

class WsHelper {

	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	private final Private proxy;

	public WsHelper(final Private proxy) {
		this.proxy = proxy;
	}

	protected Private proxy() {
		return proxy;
	}

	public static String convertToWsType(final Object value) {
		final String stringValue;
		if (value == null) {
			stringValue = EMPTY;
		} else if (value instanceof Number) {
			stringValue = Number.class.cast(value).toString();
		} else if (value instanceof Date) {
			final SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
			final Date date = Date.class.cast(value);
			stringValue = formatter.format(date);
		} else {
			stringValue = value.toString();
		}
		return stringValue;
	}

}
