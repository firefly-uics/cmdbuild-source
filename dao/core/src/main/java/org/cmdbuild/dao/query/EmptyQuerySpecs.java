package org.cmdbuild.dao.query;

import java.util.List;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.join.JoinClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;

public class EmptyQuerySpecs implements QuerySpecs {

	protected EmptyQuerySpecs() {
	}

	@Override
	public CMEntryType getFromType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Alias getFromAlias() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<JoinClause> getJoins() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<QueryAliasAttribute> getAttributes() {
		throw new UnsupportedOperationException();
	}

	@Override
	public WhereClause getWhereClause() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Long getOffset() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Long getLimit() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<OrderByClause> getOrderByClauses() {
		throw new UnsupportedOperationException();
	}
}
