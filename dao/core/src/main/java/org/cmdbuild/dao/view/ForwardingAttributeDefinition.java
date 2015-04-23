package org.cmdbuild.dao.view;

import org.cmdbuild.dao.entrytype.CMAttribute.Mode;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingAttributeDefinition extends ForwardingObject implements CMAttributeDefinition {

	@Override
	protected abstract CMAttributeDefinition delegate();

	@Override
	public String getName() {
		return delegate().getName();
	}

	@Override
	public CMEntryType getOwner() {
		return delegate().getOwner();
	}

	@Override
	public CMAttributeType<?> getType() {
		return delegate().getType();
	}

	@Override
	public String getDescription() {
		return delegate().getDescription();
	}

	@Override
	public String getDefaultValue() {
		return delegate().getDefaultValue();
	}

	@Override
	public boolean isDisplayableInList() {
		return delegate().isDisplayableInList();
	}

	@Override
	public boolean isMandatory() {
		return delegate().isMandatory();
	}

	@Override
	public boolean isUnique() {
		return delegate().isMandatory();
	}

	@Override
	public boolean isActive() {
		return delegate().isActive();
	}

	@Override
	public Mode getMode() {
		return delegate().getMode();
	}

	@Override
	public int getIndex() {
		return delegate().getIndex();
	}

	@Override
	public String getGroup() {
		return delegate().getGroup();
	}

	@Override
	public int getClassOrder() {
		return delegate().getClassOrder();
	}

	@Override
	public String getEditorType() {
		return delegate().getEditorType();
	}

	@Override
	public String getFilter() {
		return delegate().getFilter();
	}

	@Override
	public String getForeignKeyDestinationClassName() {
		return delegate().getForeignKeyDestinationClassName();
	}

}
