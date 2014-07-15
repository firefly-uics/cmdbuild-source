package org.cmdbuild.dao.entrytype.attributetype;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IpAddressAttributeType extends AbstractAttributeType<String> {

	private static final String IPV4_OCTET_REGEX = "0*(\\d|\\d\\d|1\\d\\d|2[0-4]\\d|25[0-5])";
	private static final String IPV4_OCTETS_SEPARATOR_REGEX = "\\.";
	private static final String CLASS_SEPARATOR_REGEX = "/";
	private static final String CLASS_REGEX = "0*(\\d|[1-2]\\d|3[0-2])";
	private static final String CLASS_DEFAULT = CLASS_SEPARATOR_REGEX + "32";

	private static final Pattern IPV4REGEX = Pattern.compile(EMPTY //
			+ "^" //
			+ IPV4_OCTET_REGEX + IPV4_OCTETS_SEPARATOR_REGEX //
			+ IPV4_OCTET_REGEX + IPV4_OCTETS_SEPARATOR_REGEX //
			+ IPV4_OCTET_REGEX + IPV4_OCTETS_SEPARATOR_REGEX //
			+ IPV4_OCTET_REGEX //
			+ "(" + CLASS_SEPARATOR_REGEX + CLASS_REGEX + ")*"//
			+ "$" //
	);

	public IpAddressAttributeType() {
	}

	@Override
	public void accept(final CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected String convertNotNullValue(final Object value) {
		final String stringValue = defaultIfNull(value, EMPTY).toString().trim();
		final Matcher ipv4matcher = IPV4REGEX.matcher(stringValue);
		if (isEmpty(stringValue)) {
			return null;
		} else if (ipv4matcher.matches()) {
			final String mask = ipv4matcher.group(6);
			final String returnValue;
			if (mask == null) {
				returnValue = stringValue + CLASS_DEFAULT;
			} else {
				returnValue = stringValue;
			}
			return returnValue;
		} else {
			throw illegalValue(value);
		}
	}

}
