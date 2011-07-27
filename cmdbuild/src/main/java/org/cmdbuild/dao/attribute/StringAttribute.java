package org.cmdbuild.dao.attribute;

import java.util.Map;

import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.elements.interfaces.BaseSchema;

public class StringAttribute extends TextAttribute {

	public StringAttribute(BaseSchema schema, String name, Map<String, String> meta) {
		super(schema, name, meta);
		updateTypeIfUndefined();
	}

	@Override
	public void setLength(int length) {
		super.setLength(length);
		updateTypeIfUndefined();
	}

	private void updateTypeIfUndefined() {
		if (daoType == DaoWrapperAttribute.UNDEFINED_TYPE && getLength() > 0) {
			daoType = new StringAttributeType(this.getLength());
		}
	}

	@Override
	public AttributeType getType() {
		return AttributeType.STRING;
	}
}
