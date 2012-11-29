package org.cmdbuild.dao.legacywrappers;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.UndefinedAttributeType;
import org.cmdbuild.elements.interfaces.IAttribute;

public class AttributeWrapper implements CMAttribute {

	final IAttribute attribute;

	public AttributeWrapper(final IAttribute attribute) {
		this.attribute = attribute;
	}

	@Override
	public String getName() {
		return attribute.getName();
	}

	@Override
	public String getDescription() {
		return attribute.getDescription();
	}

	@Override
	public CMEntryType getOwner() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isActive() {
		return attribute.getStatus().isActive();
	}

	@Override
	public CMAttributeType<?> getType() {
		final CMAttributeType<?> newDaoType;
		switch (attribute.getType()) {
		case BOOLEAN:
			newDaoType = new BooleanAttributeType();
			break;
		case INTEGER:
			newDaoType = new IntegerAttributeType();
			break;
		case DECIMAL:
			newDaoType = new DecimalAttributeType(attribute.getPrecision(), attribute.getScale());
			break;
		case DOUBLE:
			newDaoType = new DoubleAttributeType();
			break;
		case DATE:
			newDaoType = new DateAttributeType();
			break;
		case TIMESTAMP:
			newDaoType = new DateTimeAttributeType();
			break;
		case CHAR:
			newDaoType = new CharAttributeType();
			break;
		case STRING:
			newDaoType = new StringAttributeType(attribute.getLength());
			break;
		case TEXT:
			newDaoType = new TextAttributeType();
			break;
		case REFERENCE:
			newDaoType = new ReferenceAttributeType();
			break;
		case FOREIGNKEY:
			newDaoType = new ForeignKeyAttributeType();
			break;
		case LOOKUP:
			newDaoType = new LookupAttributeType(attribute.getLookupType().getType());
			break;
		case INET:
			newDaoType = new IpAddressAttributeType();
			break;
		case TIME:
			newDaoType = new TimeAttributeType();
			break;
		default:
			/*
			 * REGCLASS, POINT, LINESTRING, POLYGON, BINARY, INTARRAY,
			 * STRINGARRAY
			 */
			newDaoType = new UndefinedAttributeType();
		}
		return newDaoType;
	}

	@Override
	public boolean isDisplayableInList() {
		return attribute.isBaseDSP();
	}

	@Override
	public boolean isMandatory() {
		return attribute.isNotNull();
	}

	@Override
	public boolean isUnique() {
		return attribute.isUnique();
	}

	@Override
	public Mode getMode() {
		switch (attribute.getFieldMode()) {
		case HIDDEN:
			return Mode.HIDDEN;
		case READ:
			return Mode.READ;
		case WRITE:
		default:
			return Mode.WRITE;
		}
	}

}
