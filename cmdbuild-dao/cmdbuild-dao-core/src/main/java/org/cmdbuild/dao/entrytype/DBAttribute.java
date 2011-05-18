package org.cmdbuild.dao.entrytype;

import org.apache.commons.lang.Validate;

public class DBAttribute implements CMAttribute {

	CMEntryType owner;
	private final String name;

	public DBAttribute(final String name) {
		Validate.notEmpty(name);
		this.owner = null;
		this.name = name;
	}

	@Override
	public CMEntryType getOwner() {
		return owner;
	}

	@Override
	public String getName() {
		return name;
	}
}
