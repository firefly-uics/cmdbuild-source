package org.cmdbuild.dao.query.clause;

import org.cmdbuild.dao.query.clause.alias.Alias;


public class AnyAttribute extends QueryAliasAttribute {

	private AnyAttribute(final Alias entryType) {
		super(entryType, "*");
	}

	public static AnyAttribute anyAttribute(final Alias entryType) {
		return new AnyAttribute(entryType);
	}
}
