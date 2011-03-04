package org.cmdbuild.dao.attribute;

import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.proxy.LazyCard;
import org.cmdbuild.services.auth.UserContext;

public class ForeignKeyAttribute extends IntegerAttribute {

	ITable targetClass;

	public ForeignKeyAttribute(BaseSchema schema, String name) {
		super(schema, name);
	}

	@Override
	public AttributeType getType() {
		return AttributeType.FOREIGNKEY;
	}

	@Override
	protected Object convertValue(Object maybeValue) {
		final Integer intValue = convertValueToInteger(maybeValue);
		ICard destCard = null;
		if (intValue != null) {
			destCard = new LazyCard(getFKTargetClass(), intValue);
		}
		return destCard;
	}

	@Override
	protected String notNullValueToString(Object value) {
		return ((ICard)value).getDescription();
	}

	@Override
	protected String notNullValueToDBFormat(Object value) {
		return String.valueOf(((ICard)value).getId());
	}

	@Override
	public ITable getFKTargetClass() {
		return targetClass;
	}

	@Override
	public void setFKTargetClass(String value) {
		targetClass = UserContext.systemContext().tables().get(value);
	}
}
