package org.cmdbuild.dao.attribute;

import java.util.Map;

import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IPAddressAttributeType;
import org.cmdbuild.elements.interfaces.BaseSchema;

public class IPAddressAttribute extends DaoWrapperAttribute {

	private static CMAttributeType<?> IP_TYPE = new IPAddressAttributeType();

	public IPAddressAttribute(BaseSchema schema, String name, Map<String, String> meta) {
		super(schema, name, meta);
		daoType = IP_TYPE;
	}

	public AttributeType getType() {
		return AttributeType.INET;
	}

	@Override
	public String notNullValueToDBFormat(Object value) {
		return escapeAndQuote((String)value);
	}
}
