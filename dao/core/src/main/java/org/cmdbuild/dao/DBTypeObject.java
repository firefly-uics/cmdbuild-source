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

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof CMEntryType == false) {
			return false;
		}
		final CMEntryType other = CMEntryType.class.cast(obj);
		return this.id.equals(other.getId());
	}

	@Override
	public String toString() {
		return name;
	}

}
