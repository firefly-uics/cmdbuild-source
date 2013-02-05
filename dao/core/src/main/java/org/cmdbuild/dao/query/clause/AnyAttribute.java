package org.cmdbuild.dao.query.clause;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.EntryTypeAlias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;

public class AnyAttribute extends QueryAliasAttribute {

	private static final String ANY = "*";

	private AnyAttribute(final Alias entryTypeAlias) {
		super(entryTypeAlias, ANY);
	}

	public static AnyAttribute anyAttribute(final CMEntryType entryType) {
		return anyAttribute(EntryTypeAlias.canonicalAlias(entryType));
	}

	public static AnyAttribute anyAttribute(final String entryTypeName) {
		return anyAttribute(NameAlias.as(entryTypeName));
	}

	public static AnyAttribute anyAttribute(final Alias entryTypeAlias) {
		return new AnyAttribute(entryTypeAlias);
	}

}
