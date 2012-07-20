package org.cmdbuild.dao.query;

import java.util.List;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.join.JoinClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;

public class EmptyQuerySpecs implements QuerySpecs {

	protected EmptyQuerySpecs() {
	}

	public CMEntryType getFromType() {
		throw new UnsupportedOperationException();
	}

	public Alias getFromAlias() {
		throw new UnsupportedOperationException();
	}

	public List<JoinClause> getJoins() {
		throw new UnsupportedOperationException();
	}

	public Iterable<QueryAliasAttribute> getAttributes() {
		throw new UnsupportedOperationException();
	}

	public WhereClause getWhereClause() {
		throw new UnsupportedOperationException();
	}

	public Long getOffset() {
		throw new UnsupportedOperationException();
	}

	public Long getLimit() {
		throw new UnsupportedOperationException();
	}
}
