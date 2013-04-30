package org.cmdbuild.cql.sqlbuilder.attribute;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;

public class CMFakeAttribute implements CMAttribute {

	private final String name;
	private final CMEntryType entryType;
	private final CMAttributeType<?> attributeType;

	public CMFakeAttribute(final String name, final CMEntryType entryType, final CMAttributeType<?> attributeType) {
		this.name = name;
		this.entryType = entryType;
		this.attributeType = attributeType;
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public CMEntryType getOwner() {
		return entryType;
	}

	@Override
	public CMAttributeType<?> getType() {
		return attributeType;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return name;
	}

	@Override
	public boolean isSystem() {
		return false;
	}

	@Override
	public boolean isInherited() {
		return false;
	}

	@Override
	public boolean isDisplayableInList() {
		return false;
	}

	@Override
	public boolean isMandatory() {
		return false;
	}

	@Override
	public boolean isUnique() {
		return false;
	}

	@Override
	public Mode getMode() {
		return Mode.WRITE;
	}

	@Override
	public int getIndex() {
		return 0;
	}

	@Override
	public String getDefaultValue() {
		return null;
	}

	@Override
	public String getGroup() {
		return null;
	}

	@Override
	public int getClassOrder() {
		return 0;
	}

	@Override
	public String getEditorType() {
		return null;
	}

	@Override
	public String getForeignKeyDestinationClassName() {
		return null;
	}

}
