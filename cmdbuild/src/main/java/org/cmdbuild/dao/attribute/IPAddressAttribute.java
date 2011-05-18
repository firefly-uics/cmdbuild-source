package org.cmdbuild.dao.attribute;

import java.util.regex.Pattern;

import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.exception.ORMException.ORMExceptionType;

public class IPAddressAttribute extends AttributeImpl {

	private static final Pattern IPV4REGEX = Pattern.compile("^0*([1-9]?\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.0*([1-9]?\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.0*([1-9]?\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.0*([1-9]?\\d|1\\d\\d|2[0-4]\\d|25[0-5])$");

	public IPAddressAttribute(BaseSchema schema, String name) {
		super(schema, name);
	}

	public AttributeType getType() {
		return AttributeType.INET;
	}

	@Override
	protected Object convertValue(Object value) {
		if (value instanceof String) {
			final String stringValue = ((String)value).trim();
			if (stringValue.isEmpty()) {
				return null;
			} else if (IPV4REGEX.matcher(stringValue).find()) {
				return stringValue;
			}
		}
		throw ORMExceptionType.ORM_TYPE_ERROR.createException();
	}

	@Override
	public String notNullValueToDBFormat(Object value) {
		return escapeAndQuote((String)value);
	}
}
