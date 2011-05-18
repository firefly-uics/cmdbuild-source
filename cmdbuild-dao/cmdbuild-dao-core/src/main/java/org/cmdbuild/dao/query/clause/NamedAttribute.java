package org.cmdbuild.dao.query.clause;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;

/**
 * This represents an attribute that is not bound
 * to any class, but only identified by its name
 */
public class NamedAttribute implements CMAttribute {

	final String name;

	public NamedAttribute(final String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public CMEntryType getOwner() {
		return null;
	}
}
