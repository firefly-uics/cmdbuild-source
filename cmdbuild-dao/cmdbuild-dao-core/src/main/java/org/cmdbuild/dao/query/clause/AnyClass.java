package org.cmdbuild.dao.query.clause;

import java.util.Set;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.PlaceholderClass;

public class AnyClass extends PlaceholderClass {

	private AnyClass() {
	}

	public static CMClass anyClass() {
		return new AnyClass();
	}

	@Override
	public String getName() {
		return "*";
	}

	@Override
	public boolean isAncestorOf(CMClass cmClass) {
		throw new UnsupportedOperationException();
	}
}
