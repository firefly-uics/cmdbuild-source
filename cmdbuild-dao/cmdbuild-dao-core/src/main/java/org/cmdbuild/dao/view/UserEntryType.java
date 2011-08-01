package org.cmdbuild.dao.view;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.DBEntryType;

public abstract class UserEntryType implements CMEntryType {

	protected final UserDataView view;

	/*
	 * Should be used by the subclasses only
	 */
	protected UserEntryType(final UserDataView view) {
		this.view = view;
	}

	protected abstract DBEntryType inner();

	@Override
	public boolean isActive() {
		return inner().isActive();
	}

	@Override
	public Object getId() {
		return inner().getId();
	}

	@Override
	public String getName() {
		return inner().getName();
	}

	@Override
	public String getDescription() {
		return inner().getDescription();
	}

	@Override
	public boolean isSystem() {
		return inner().isSystem();
	}

	@Override
	public Iterable<UserAttribute> getAttributes() {
		return view.proxyAttributes(inner().getAttributes());
	}

	@Override
	public Iterable<UserAttribute> getAllAttributes() {
		return view.proxyAttributes(inner().getAllAttributes());
	}

	@Override
	public UserAttribute getAttribute(String name) {
		return UserAttribute.create(view, inner().getAttribute(name));
	}

	/*
	 * Object overrides
	 */

	@Override
	public int hashCode() {
		return inner().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return inner().equals(obj);
	}

	@Override
	public String toString() {
		// TODO Add username
		return inner().toString();
	}
}
