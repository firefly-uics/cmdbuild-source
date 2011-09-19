package org.cmdbuild.dao.entrytype.attributetype;

import java.util.regex.Pattern;

// TODO Change to CMAttributeType<InetAddress>
public class IPAddressAttributeType implements CMAttributeType<String> {

	private static final Pattern IPV4REGEX = Pattern.compile("^0*([1-9]?\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.0*([1-9]?\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.0*([1-9]?\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.0*([1-9]?\\d|1\\d\\d|2[0-4]\\d|25[0-5])$");

	public IPAddressAttributeType() {
	}

	@Override
	public void accept(CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String convertNotNullValue(Object value) {
		final String stringValue = value.toString().trim();
		if (stringValue.isEmpty()) {
			return null;
		} else if (IPV4REGEX.matcher(stringValue).find()) {
			return stringValue;
		} else {
			throw new IllegalArgumentException();
		}
	}
}
