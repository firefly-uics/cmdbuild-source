package org.cmdbuild.dao.view.user.privileges;

import java.util.Map;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.where.WhereClause;

public abstract class ForwardingRowAndColumnPrivilegeFetcher implements RowAndColumnPrivilegeFetcher {

	private final RowAndColumnPrivilegeFetcher delegate;

	protected ForwardingRowAndColumnPrivilegeFetcher(final RowAndColumnPrivilegeFetcher delegate) {
		this.delegate = delegate;
	}

	@Override
	public Iterable<? extends WhereClause> fetchPrivilegeFiltersFor(final CMEntryType entryType) {
		return delegate.fetchPrivilegeFiltersFor(entryType);
	}

	@Override
	public Iterable<? extends WhereClause> fetchPrivilegeFiltersFor(final CMEntryType entryType,
			final CMEntryType entryTypeForClauses) {
		return delegate.fetchPrivilegeFiltersFor(entryType, entryTypeForClauses);
	}

	@Override
	public Map<String, String> fetchAttributesPrivilegesFor(final CMEntryType entryType) {
		return delegate.fetchAttributesPrivilegesFor(entryType);
	}

}
