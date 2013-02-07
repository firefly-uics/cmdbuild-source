package org.cmdbuild.dao.query;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.from.FromClause;
import org.cmdbuild.dao.query.clause.join.JoinClause;
import org.cmdbuild.dao.query.clause.where.EmptyWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;

/**
 * Used by QuerySpecsBuilder and by driver tests only
 */
public class QuerySpecsImpl implements QuerySpecs {

	private final FromClause fromClause;
	private final List<JoinClause> joinClauses;
	private final List<QueryAliasAttribute> attributes;
	private final List<OrderByClause> orderByClauses;
	private Long offset;
	private Long limit;
	private WhereClause whereClause;
	private final boolean distinct;

	public QuerySpecsImpl(final FromClause fromClause, final boolean distinct) {
		this.fromClause = fromClause;
		joinClauses = new ArrayList<JoinClause>();
		attributes = new ArrayList<QueryAliasAttribute>();
		orderByClauses = new ArrayList<OrderByClause>();
		offset = null;
		limit = null;
		whereClause = new EmptyWhereClause();
		this.distinct = distinct;
	}

	@Override
	public FromClause getFromClause() {
		return fromClause;
	}

	public void addJoin(final JoinClause jc) {
		joinClauses.add(jc);
	}

	@Override
	public List<JoinClause> getJoins() {
		return joinClauses;
	}

	@Override
	public Iterable<QueryAliasAttribute> getAttributes() {
		return this.attributes;
	}

	public void addOrderByClause(final OrderByClause orderByClause) {
		this.orderByClauses.add(orderByClause);
	}

	@Override
	public List<OrderByClause> getOrderByClauses() {
		return orderByClauses;
	}

	public void addSelectAttribute(final QueryAliasAttribute attribute) {
		attributes.add(attribute);
	}

	public void setWhereClause(final WhereClause whereClause) {
		this.whereClause = whereClause;
	}

	@Override
	public WhereClause getWhereClause() {
		return whereClause;
	}

	public void setOffset(final Long offset) {
		this.offset = offset;
	}

	@Override
	public Long getOffset() {
		return offset;
	}

	public void setLimit(final Long limit) {
		this.limit = limit;
	}

	@Override
	public Long getLimit() {
		return limit;
	}

	@Override
	public boolean distinct() {
		return distinct;
	}

}
