package org.cmdbuild.dao.query.clause;

import org.cmdbuild.dao.entrytype.PlaceholderClass;


/**
 * This represents a class that has to be checked at runtime
 */
public class NamedClass extends PlaceholderClass {

	final String name;

	public NamedClass(final String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}
