package org.cmdbuild.dao.view.user;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

public class UserAttribute implements CMAttribute {

	private final UserDataView view;
	private final DBAttribute inner;

	static UserAttribute create(final UserDataView view, final DBAttribute inner) {
		if (inner != null && inner.isActive() || view.getAccessControlManager().hasDatabaseDesignerPrivileges()) {
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
	public boolean isActive() {
		return inner.isActive();
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

	/*
	 * Object overrides
	 */

	@Override
	public int hashCode() {
		return inner.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return inner.equals(obj);
	}

	@Override
	public String toString() {
		// TODO Add username
		return inner.toString();
	}
}
