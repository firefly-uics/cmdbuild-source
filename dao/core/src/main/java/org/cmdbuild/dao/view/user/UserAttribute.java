package org.cmdbuild.dao.view.user;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

public class UserAttribute implements CMAttribute {

	private final UserDataView view;
	private final DBAttribute inner;

	static UserAttribute newInstance(final UserDataView view, final DBAttribute inner) {
		if (inner != null) {
			return new UserAttribute(view, inner);
		} else {
			return null;
		}
	}

	private UserAttribute(final UserDataView view, final DBAttribute inner) {
		this.view = view;
		this.inner = inner;
	}

	@Override
	public UserEntryType getOwner() {
		return view.proxy(inner.getOwner());
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
	public boolean isInherited() {
		return inner.isInherited();
	}

	@Override
	public boolean isActive() {
		return inner.isActive();
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
	public String getForeignKeyDestinationClassName() {
		return inner.getForeignKeyDestinationClassName();
	}

	/*
	 * Object overrides
	 */

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
		// TODO Add username
		return inner.toString();
	}

}
