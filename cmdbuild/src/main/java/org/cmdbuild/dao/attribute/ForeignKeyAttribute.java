package org.cmdbuild.dao.attribute;

import java.util.Map;

import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.proxy.LazyCard;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;

public class ForeignKeyAttribute extends AttributeImpl {

	ITable targetClass;

	public ForeignKeyAttribute(final BaseSchema schema, final String name, final Map<String, String> meta) {
		super(schema, name, meta);
	}

	@Override
	public AttributeType getType() {
		return AttributeType.FOREIGNKEY;
	}

	@Override
	protected ICard convertValue(Object maybeValue) {
		try {
			final Integer intValue = IntegerAttribute.INTEGER_TYPE.convertValue(maybeValue);
			ICard destCard = null;
			if (intValue != null) {
				destCard = new LazyCard(getFKTargetClass(), intValue);
			}
			return destCard;
		} catch (final Throwable t) {
			throw ORMExceptionType.ORM_TYPE_ERROR.createException();
		}
	}

	@Override
	protected String notNullValueToString(final Object value) {
		return ((ICard) value).getDescription();
	}

	@Override
	protected String notNullValueToDBFormat(final Object value) {
		return String.valueOf(((ICard) value).getId());
	}

	@Override
	public ITable getFKTargetClass() {
		return targetClass;
	}

	@Override
	public void setFKTargetClass(final String value) {
		targetClass = UserOperations.from(UserContext.systemContext()).tables().get(value);
	}
}
