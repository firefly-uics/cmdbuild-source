package org.cmdbuild.dao.query.clause;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.alias.Alias;

public class AnyAttribute extends QueryAliasAttribute {

	private static final String ANY = "*";

	private AnyAttribute(final Alias entryType) {
		super(entryType, ANY);
	}

	public static AnyAttribute anyAttribute(final CMEntryType entryType) {
		return anyAttribute(Alias.canonicalAlias(entryType));
	}

	public static AnyAttribute anyAttribute(final String entryTypeName) {
		return anyAttribute(Alias.as(entryTypeName));
	}

	public static AnyAttribute anyAttribute(final Alias entryTypeAlias) {
		return new AnyAttribute(entryTypeAlias);
	}

}
