package org.cmdbuild.dao.driver.postgres;

import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

/**
 * A simple DTO that contains the value and type
 */
class AttributeValueType {
	
	/**
	 * unquoted attribute name
	 */
	private String name;
	private Object value;
	private CMAttributeType<?> type;

	AttributeValueType(String name, Object value, CMAttributeType<?> type) {
		this.name = name;
		this.value = value;
		this.type = type;
	}

	String getName() {
		return name;
	}

	Object getValue() {
		return value;
	}

	CMAttributeType<?> getType() {
		return type;
	}
	
}
