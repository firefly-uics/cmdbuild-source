package org.cmdbuild.dao.query;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.join.JoinClause;
import org.cmdbuild.dao.query.clause.where.EmptyWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;

/**
 * Used by QuerySpecsBuilder and by driver tests only
 */
public class QuerySpecsImpl implements QuerySpecs {

	private final CMEntryType fromType;
	private final Alias fromAlias;
	private final List<JoinClause> joinClauses;
	private final List<QueryAliasAttribute> attributes;
	private final List<OrderByClause> orderByClauses;
	private Long offset;
	private Long limit;
	private WhereClause whereClause;

	public QuerySpecsImpl(final CMEntryType fromType, final  Alias fromAlias) {
		this.fromType = fromType;
		this.fromAlias = fromAlias;
		joinClauses = new ArrayList<JoinClause>();
		attributes = new ArrayList<QueryAliasAttribute>();
		orderByClauses = new ArrayList<OrderByClause>();
		offset = null;
		limit = null;
		whereClause = new EmptyWhereClause();
	}

	public CMEntryType getFromType() {
		return fromType;
	}

	public Alias getFromAlias() {
		return fromAlias;
	}

	public void addJoin(final JoinClause jc) {
		joinClauses.add(jc);
	}

	public List<JoinClause> getJoins() {
		return joinClauses;
	}

	public Iterable<QueryAliasAttribute> getAttributes() {
		return this.attributes;
	}

	public void addOrderByClause(OrderByClause orderByClause) {
		this.orderByClauses.add(orderByClause);
	}

	public List<OrderByClause> getOrderByClauses() {
		return orderByClauses;
	}

	public void addSelectAttribute(final QueryAliasAttribute attribute) {
		attributes.add(attribute);
	}

	public void setWhereClause(final WhereClause whereClause) {
		this.whereClause = whereClause;
	}

	public WhereClause getWhereClause() {
		return whereClause;
	}

	public void setOffset(final Long offset) {
		this.offset = offset;
	}

	public Long getOffset() {
		return offset;
	}

	public void setLimit(final Long limit) {
		this.limit = limit;
	}

	public Long getLimit() {
		return limit;
	}
}
