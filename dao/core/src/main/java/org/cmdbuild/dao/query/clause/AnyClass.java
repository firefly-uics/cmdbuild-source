package org.cmdbuild.dao.query.clause;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.PlaceholderClass;

public class AnyClass extends PlaceholderClass {

	private static final AnyClass ANY_CLASS = new AnyClass();

	private AnyClass() {
	}

	public static CMClass anyClass() {
		return ANY_CLASS;
	}

	/*
	 * Object overrides
	 */

	@Override
	public String toString() {
		return "*";
	}
}
