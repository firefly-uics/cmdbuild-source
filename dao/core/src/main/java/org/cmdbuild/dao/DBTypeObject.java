package org.cmdbuild.dao;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.CMEntryType;

public abstract class DBTypeObject implements CMTypeObject {

	private final Long id;
	private final String name;

	protected DBTypeObject(final String name, final Long id) {
		Validate.notEmpty(name);
		this.id = id;
		this.name = name;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	/*
	 * Object overrides
	 */

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CMEntryType == false)
			return false;
		if (this == obj)
			return true;
		CMEntryType other = (CMEntryType) obj;
		return this.id.equals(other.getId());
	}

	@Override
	public String toString() {
		return name;
	}
}
