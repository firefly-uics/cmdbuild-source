package org.cmdbuild.service.rest.dto;

public enum AttributeType {

	BOOLEAN(Constants.TYPE_BOOLEAN), //
	CHAR(Constants.TYPE_CHAR), //
	DATE(Constants.TYPE_DATE), //
	DATE_TIME(Constants.TYPE_DATE_TIME), //
	DOUBLE(Constants.TYPE_DOUBLE), //
	DECIMAL(Constants.TYPE_DECIMAL), //
	ENTRY_TYPE(Constants.TYPE_ENTRY_TYPE), //
	FOREIGN_KEY(Constants.TYPE_FOREIGN_KEY), //
	INTEGER(Constants.TYPE_INTEGER), //
	IP_ADDRESS(Constants.TYPE_IP_ADDRESS), //
	LOOKUP(Constants.TYPE_LOOKUP), //
	REFERENCE(Constants.TYPE_REFERENCE), //
	STRING(Constants.TYPE_STRING), //
	STRING_ARRAY(Constants.TYPE_STRING_ARRAY), //
	TEXT(Constants.TYPE_TEXT), //
	TIME(Constants.TYPE_TIME), //
	;

	private final String asString;

	private AttributeType(final String asString) {
		this.asString = asString;
	}

	public String asString() {
		return asString;
	}

}
