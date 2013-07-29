package org.cmdbuild.report;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;

public class ReportFakeAttribute implements CMAttribute {

	private final String name;
	private final String description;

	public ReportFakeAttribute( //
			final String name, //
			final String description) {

		this.name = name;
		this.description = description;
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public CMEntryType getOwner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CMAttributeType<?> getType() {
		return new StringAttributeType(100);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
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
		return true;
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
		return null;
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

	@Override
	public String getFilter() {
		return "";
	}
}
