package org.cmdbuild.dao.attribute;

import java.util.Map;

import org.cmdbuild.dao.reference.CardReference;
import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.proxy.LazyCard;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.services.auth.UserContext;

public class ForeignKeyAttribute extends AttributeImpl {

	ITable targetClass;

	public ForeignKeyAttribute(BaseSchema schema, String name, Map<String, String> meta) {
		super(schema, name, meta);
	}

	@Override
	public AttributeType getType() {
		return AttributeType.FOREIGNKEY;
	}

	@Override
	protected ICard convertValue(Object maybeValue) {
		if (maybeValue instanceof CardReference) {
			maybeValue = ((CardReference) maybeValue).getId();
		}
		try {
			final Integer intValue = IntegerAttribute.INTEGER_TYPE.convertValue(maybeValue);
			ICard destCard = null;
			if (intValue != null) {
				destCard = new LazyCard(getFKTargetClass(), intValue);
			}
			return destCard;
		} catch (Throwable t) {
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		}
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
