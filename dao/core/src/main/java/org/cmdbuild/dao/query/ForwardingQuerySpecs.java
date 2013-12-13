package org.cmdbuild.dao.query;

import java.util.List;

import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.from.FromClause;
import org.cmdbuild.dao.query.clause.join.DirectJoinClause;
import org.cmdbuild.dao.query.clause.join.JoinClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;

public class ForwardingQuerySpecs implements QuerySpecs {

	private final QuerySpecs inner;

	public ForwardingQuerySpecs(final QuerySpecs querySpecs) {
		this.inner = querySpecs;
	}

	@Override
	public FromClause getFromClause() {
		return inner.getFromClause();
	}

	@Override
	public List<JoinClause> getJoins() {
		return inner.getJoins();
	}

	@Override
	public List<DirectJoinClause> getDirectJoins() {
		return inner.getDirectJoins();
	}

	@Override
	public List<OrderByClause> getOrderByClauses() {
		return inner.getOrderByClauses();
	}

	@Override
	public Iterable<QueryAliasAttribute> getAttributes() {
		return inner.getAttributes();
	}

	@Override
	public WhereClause getWhereClause() {
		return inner.getWhereClause();
	}

	@Override
	public Long getOffset() {
		return inner.getOffset();
	}

	@Override
	public Long getLimit() {
		return inner.getLimit();
	}

	@Override
	public boolean distinct() {
		return inner.distinct();
	}

	@Override
	public boolean numbered() {
		return inner.numbered();
	}

	@Override
	public WhereClause getConditionOnNumberedQuery() {
		return inner.getConditionOnNumberedQuery();
	}

	@Override
	public boolean count() {
		return inner.count();
	}

}
