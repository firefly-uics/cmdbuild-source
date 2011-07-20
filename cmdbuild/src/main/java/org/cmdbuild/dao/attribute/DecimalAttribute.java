package org.cmdbuild.dao.attribute;

import java.util.Map;

import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.elements.interfaces.BaseSchema;

public class DecimalAttribute extends DaoWrapperAttribute {

	public DecimalAttribute(BaseSchema schema, String name, Map<String, String> meta) {
		super(schema, name, meta);
		updateTypeIfUndefined();
	}

	@Override
	public void setPrecision(int precision) {
		super.setPrecision(precision);
		updateTypeIfUndefined();
	}

	@Override
	public void setScale(int scale) {
		super.setScale(scale);
		updateTypeIfUndefined();
	}

	private void updateTypeIfUndefined() {
		if (daoType == DaoWrapperAttribute.UNDEFINED_TYPE && getPrecision() > 0 && getScale() > 0) {
			daoType = new DecimalAttributeType(getPrecision(), getScale());
		}
	}

	@Override
	public AttributeType getType() {
		return AttributeType.DECIMAL;
	}
}
