package org.cmdbuild.dao.attribute;

import java.util.Map;

import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.elements.interfaces.BaseSchema;

public class TextAttribute extends DaoWrapperAttribute {

	private static CMAttributeType<?> TEXT_TYPE = new TextAttributeType();

	public TextAttribute(BaseSchema schema, String name, Map<String, String> meta) {
		super(schema, name, meta);
		daoType = TEXT_TYPE;
	}

	@Override
	public AttributeType getType() {
		return AttributeType.TEXT;
	}

	@Override
	public String notNullValueToDBFormat(Object value) {
		return escapeAndQuote((String)value);
	}
}
