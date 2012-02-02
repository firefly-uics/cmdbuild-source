package org.cmdbuild.dao.query;

import static org.cmdbuild.dao.query.clause.alias.UndefinedClassAlias.UNDEFINED_CLASS_ALIAS;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.ClassAlias;
import org.cmdbuild.dao.query.clause.join.JoinClause;
import org.cmdbuild.dao.query.clause.where.EmptyWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;

/*
 * Used by QuerySpecsBuilder and by driver tests only
 */
public class QuerySpecsImpl implements QuerySpecs {

	private final List<QueryAliasAttribute> attributes;
	private ClassAlias from;
	private final List<JoinClause> joinClauses;
	private Long offset;
	private Long limit;
	private WhereClause whereClause;

	protected QuerySpecsImpl() {
		attributes = new ArrayList<QueryAliasAttribute>();
		from = UNDEFINED_CLASS_ALIAS;
		joinClauses = new ArrayList<JoinClause>();
		whereClause = new EmptyWhereClause();
		offset = null;
		limit = null;
	}

	public void setFrom(final ClassAlias from) {
		this.from = from;
	}

	public ClassAlias getDBFrom() {
		return from;
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
