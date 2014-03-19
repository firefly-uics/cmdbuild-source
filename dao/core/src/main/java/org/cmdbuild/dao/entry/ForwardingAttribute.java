package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

public class ForwardingAttribute implements CMAttribute {

	private final CMAttribute inner;

	public ForwardingAttribute(final CMAttribute inner) {
		this.inner = inner;
	}

	@Override
	public boolean isActive() {
		return inner.isActive();
	}

	@Override
	public CMEntryType getOwner() {
		return inner.getOwner();
	}

	@Override
	public CMAttributeType<?> getType() {
		return inner.getType();
	}

	@Override
	public String getName() {
		return inner.getName();
	}

	@Override
	public String getDescription() {
		return inner.getDescription();
	}

	@Override
	public boolean isSystem() {
		return inner.isSystem();
	}

	@Override
	public boolean isInherited() {
		return inner.isInherited();
	}

	@Override
	public boolean isDisplayableInList() {
		return inner.isDisplayableInList();
	}

	@Override
	public boolean isMandatory() {
		return inner.isMandatory();
	}

	@Override
	public boolean isUnique() {
		return inner.isUnique();
	}

	@Override
	public Mode getMode() {
		return inner.getMode();
	}

	@Override
	public int getIndex() {
		return inner.getIndex();
	}

	@Override
	public String getDefaultValue() {
		return inner.getDefaultValue();
	}

	@Override
	public String getGroup() {
		return inner.getGroup();
	}

	@Override
	public int getClassOrder() {
		return inner.getClassOrder();
	}

	@Override
	public String getEditorType() {
		return inner.getEditorType();
	}

	@Override
	public String getFilter() {
		return inner.getFilter();
	}

	@Override
	public String getForeignKeyDestinationClassName() {
		return inner.getForeignKeyDestinationClassName();
	}

	@Override
	public int hashCode() {
		return inner.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		return inner.equals(obj);
	}

	@Override
	public String toString() {
		return inner.toString();
	}

}
