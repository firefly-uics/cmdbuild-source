package org.cmdbuild.dao.query.clause;

public class Attributes {

	public static QueryAttribute named(final String value) {
		return new NamedAttribute(value);
	}

	/**
	 * Prevents instantiation.
	 */
	private Attributes() {
	}

}
