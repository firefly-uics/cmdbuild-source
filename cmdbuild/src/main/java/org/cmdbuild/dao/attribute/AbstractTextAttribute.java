package org.cmdbuild.dao.attribute;

import java.util.Map;

import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.elements.interfaces.BaseSchema;

public abstract class AbstractTextAttribute extends DaoWrapperAttribute {

	protected static CMAttributeType<?> TEXT_TYPE = new TextAttributeType();

	public AbstractTextAttribute(BaseSchema schema, String name, Map<String, String> meta) {
		super(schema, name, meta);
		daoType = TEXT_TYPE; // For backward compatibility but... why?!
	}

	@Override
	public final String notNullValueToDBFormat(Object value) {
		return escapeAndQuote((String)value);
	}
}
