package org.cmdbuild.dao.query;

import java.util.List;

import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.from.FromClause;
import org.cmdbuild.dao.query.clause.join.JoinClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;

public class ForwardingQuerySpecs implements QuerySpecs {

	private final QuerySpecs querySpecs;

	public ForwardingQuerySpecs(final QuerySpecs querySpecs) {
		this.querySpecs = querySpecs;
	}

	@Override
	public FromClause getFromClause() {
		return querySpecs.getFromClause();
	}

	@Override
	public List<JoinClause> getJoins() {
		return querySpecs.getJoins();
	}

	@Override
	public List<OrderByClause> getOrderByClauses() {
		return querySpecs.getOrderByClauses();
	}

	@Override
	public Iterable<QueryAliasAttribute> getAttributes() {
		return querySpecs.getAttributes();
	}

	@Override
	public WhereClause getWhereClause() {
		return querySpecs.getWhereClause();
	}

	@Override
	public Long getOffset() {
		return querySpecs.getOffset();
	}

	@Override
	public Long getLimit() {
		return querySpecs.getLimit();
	}

	@Override
	public boolean distinct() {
		return querySpecs.distinct();
	}

	@Override
	public boolean numbered() {
		return querySpecs.numbered();
	}

	@Override
	public WhereClause getConditionOnNumberedQuery() {
		return querySpecs.getConditionOnNumberedQuery();
	}

}
