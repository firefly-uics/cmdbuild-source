package org.cmdbuild.dao.view.user.privileges;

import java.util.Map;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.where.WhereClause;

import com.google.common.collect.ForwardingObject;

public abstract class ForwardingRowAndColumnPrivilegeFetcher extends ForwardingObject implements
		RowAndColumnPrivilegeFetcher {

	/**
	 * Usable by subclasses only.
	 */
	protected ForwardingRowAndColumnPrivilegeFetcher() {
	}

	@Override
	protected abstract RowAndColumnPrivilegeFetcher delegate();

	@Override
	public Iterable<? extends WhereClause> fetchPrivilegeFiltersFor(final CMEntryType entryType) {
		return delegate().fetchPrivilegeFiltersFor(entryType);
	}

	@Override
	public Iterable<? extends WhereClause> fetchPrivilegeFiltersFor(final CMEntryType entryType,
			final CMEntryType entryTypeForClauses) {
		return delegate().fetchPrivilegeFiltersFor(entryType, entryTypeForClauses);
	}

	@Override
	public Iterable<? extends WhereClause> fetchPrivilegeFiltersFor(final CMEntryType entryType,
			final CMEntryType entryTypeForClauses, final Alias entryTypeForClausesAlias) {
		return delegate().fetchPrivilegeFiltersFor(entryType, entryTypeForClauses, entryTypeForClausesAlias);
	}

	@Override
	public Map<String, String> fetchAttributesPrivilegesFor(final CMEntryType entryType) {
		return delegate().fetchAttributesPrivilegesFor(entryType);
	}

}
