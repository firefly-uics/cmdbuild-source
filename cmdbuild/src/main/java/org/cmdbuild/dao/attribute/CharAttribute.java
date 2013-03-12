package org.cmdbuild.dao.attribute;

import java.util.Map;

import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.elements.interfaces.BaseSchema;

public class CharAttribute extends AbstractTextAttribute {

	private static CMAttributeType<?> CHAR_TYPE = new CharAttributeType();

	public CharAttribute(BaseSchema schema, String name, Map<String, String> meta) {
		super(schema, name, meta);
		daoType = CHAR_TYPE;
	}

	@Override
	public AttributeType getType() {
		return AttributeType.CHAR;
	}
}
