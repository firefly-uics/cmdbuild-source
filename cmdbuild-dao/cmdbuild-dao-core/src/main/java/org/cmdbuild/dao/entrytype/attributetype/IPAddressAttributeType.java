package org.cmdbuild.dao.entrytype.attributetype;

import java.util.regex.Pattern;


public class IPAddressAttributeType implements CMAttributeType<String> {

	private static final Pattern IPV4REGEX = Pattern.compile("^0*([1-9]?\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.0*([1-9]?\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.0*([1-9]?\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.0*([1-9]?\\d|1\\d\\d|2[0-4]\\d|25[0-5])$");

	public IPAddressAttributeType() {
	}

	@Override
	public String convertNotNullValue(Object value) {
		if (value instanceof String) {
			final String stringValue = ((String)value).trim();
			if (stringValue.isEmpty()) {
				return null;
			} else if (IPV4REGEX.matcher(stringValue).find()) {
				return stringValue;
			}
		}
		throw new IllegalArgumentException();
	}
}
